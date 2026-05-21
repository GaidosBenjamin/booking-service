# Booking Service — Functional Overview

A multi-tenant camp booking platform. Organizations onboard with a unique slug, parents register and pay to assign their children (campers) to rooms. Payment is handled via Stripe Checkout. Admins manage buildings, rooms, tiers, and leaders.

---

## Multi-Tenancy

Every entity is scoped to a `tenantId` (UUID, FK to `Organization`). Users log in with an `organizationSlug` + `email` + `password`. The JWT carries a `tid` (tenantId) claim; all DB queries filter by it. There is no cross-tenant data access.

### Organization Bootstrap
`POST /api/auth/onboarding` creates an `Organization` and two default roles: `DEFAULT` and `ADMIN`. The first user to register in that organization is automatically assigned both roles; all subsequent users get `DEFAULT` only.

---

## Auth

- **Register** → creates `User` (emailVerified=false) + `UserProfile`, assigns role, sends email verification code (15 min TTL).
- **Verify email** → sets `emailVerified=true`. Login is blocked until verified.
- **Login** → validates credentials, returns JWT (60 min) + refresh token (30 days).
- **Refresh** → old refresh token revoked, new one issued (rotation). `familyId` is stable per login session to allow multi-device use.
- **Forgot/reset password** → token hash stored, 30 min TTL, single-use (`consumedAt` field).

### Permissions
Roles carry a list of permission strings (`resource:action`). Controllers enforce them via `@PreAuthorize("hasAuthority('...')")`.

**DEFAULT role permissions:** `campers:read/write`, `tiers:read`, `buildings:read`, `leaders:read`, `rooms:read`, `rooms:holds:write`, `rooms:assignments:read`, `conduct:read`, `bookings:read/write`

**ADMIN role permissions (additive):** `tiers:write`, `buildings:write`, `leaders:write`, `rooms:write`, `rooms:assignments:write`, `conduct:write`

Special authority `ROLE_ADMIN` gates force-delete and other admin-only endpoints.

---

## Core Entities & Relationships

```
Organization
└── User (parent/guardian)
    └── Camper (child being registered)
        ├── RoomHold (temporary, expires in 15 min)
        └── RoomAssignment (permanent, created after payment)

Building (e.g. dormitory)
└── Room (capacity, gender/age restrictions)
    └── Tier (pricing: basePrice, discountPrice)

Booking (Stripe session)
└── BookingItem (one per camper: captures camper, room, tier, price at booking time)
```

### Camper statuses
| Status | Meaning |
|---|---|
| `NEEDS_BED` | No active room hold |
| `NEEDS_PAYMENT` | Has an active `RoomHold` (dynamically computed) |
| `PAYMENT_SUCCESS` | Payment completed, `RoomAssignment` exists |
| `PAYMENT_FAILED` | Stripe payment failed |

Status is partially computed at read time: if a camper's stored status is not a terminal state (`PAYMENT_SUCCESS`/`PAYMENT_FAILED`), it is derived from whether an active hold exists.

### RoomHold
Temporary reservation (default 15 min, configurable via `app.room-hold.ttl`). One per camper (unique constraint). Capacity check: `activeHolds + assignments < room.capacity`. A hold is "active" only if `expiresAt > now`; expired holds remain in DB but are filtered out of all queries.

When a booking is created, the hold's `expiresAt` is extended to match the Stripe session expiry (30 min), preventing the room from being released while payment is pending.

### Room compatibility rules
A camper can only hold/be assigned to a room if:
- `room.allowedGender` is null OR equals `camper.gender`
- Camper's age is within `room.minAge`–`room.maxAge` (if set)
- `room.leaderRoom = false`

### RoomAssignment
Permanent after payment. Can reference a `Camper` OR a `Leader`, never both (XOR constraint). Created automatically via Stripe webhook.

### Tier & Pricing
Buildings are associated with a `Tier`. Tiers have `basePrice` and `discountPrice`. A parent is charged `discountPrice` if their email or phone appears in the `Member` table; otherwise `basePrice`. Price is captured in `BookingItem.price` at checkout time, so later tier changes or soft-deletes don't affect past bookings.

---

## Booking & Payment Flow

### 1. Parent creates a camper
`POST /api/campers` — status starts as `NEEDS_BED`.

### 2. Parent browses and picks a room
`GET /api/rooms?gender=male&age=10` — returns rooms filtered by availability, gender, age. `GET /api/buildings?gender=male&age=10` — returns buildings with eligible tiers.

### 3. Parent creates a room hold
`POST /api/rooms/{roomId}/holds` with `camperId`. If camper already holds a different room, the hold is moved. `expiresAt = now + 15 min`.

### 4. Parent creates a booking (Stripe checkout)
`POST /api/bookings` — optionally accepts `camperIds` to filter which held campers to include.

**Steps:**
1. Resolves all active room holds for the user (filtered by camperIds if provided).
2. Validates no camper already has a `PENDING` or `SUCCEEDED` booking.
3. Checks `Member` table for discount eligibility.
4. Creates Stripe Checkout session (outside DB transaction): one line item per camper, `expiresAt = now + 30 min`.
5. Saves `Booking` (status=`PENDING`) and one `BookingItem` per camper.
6. Extends all involved room holds to match Stripe session expiry.

Returns `checkoutUrl` for redirect to Stripe.

### 5. Stripe webhook confirms payment
`POST /api/webhooks/stripe` — verified via Stripe signature.

**On success** (`checkout.session.completed` or `checkout.session.async_payment_succeeded`):
- Atomic CAS update: `status PENDING → SUCCEEDED` (idempotent; duplicate webhooks are no-ops).
- For each `BookingItem`: deletes `RoomHold`, creates `RoomAssignment`, sets `Camper.status = PAYMENT_SUCCESS`.
- Publishes `BookingConfirmedEvent`.

**On expiry/failure** (`checkout.session.expired` or `checkout.session.async_payment_failed`):
- Sets `Booking.status = CANCELED` or `FAILED`.
- If failed: sets `Camper.status = PAYMENT_FAILED`.

### 6. Cancellation
`POST /api/bookings/{id}/cancel` — only works on `PENDING` bookings (cannot cancel `SUCCEEDED`). Calls `Session.expire()` on Stripe, resets room holds to 15 min.

---

## Camper Deletion

**Normal delete** (`DELETE /api/campers/{id}`):
- Blocked if `RoomAssignment` exists.
- Blocked if `PENDING` booking exists.
- Cleans up dead booking items (FAILED/CANCELED) and room holds.

**Force delete** (`DELETE /api/campers/{id}/force`, requires `ROLE_ADMIN`):
- Deletes `RoomAssignment`, all `BookingItem` records, room holds, then the camper.
- The parent `Booking` record is **not deleted** — it serves as a financial audit trail (holds Stripe session ID and amount).

---

## Donations

Separate payment flow, no auth required.

`POST /api/donations` — accepts `name` (optional), `orgSlug`, `amount`, `currency`. Creates a Stripe Checkout session with `submitType=DONATE`. On webhook success, `Donation.status → SUCCEEDED`. No camper or room involvement.

---

## Room Assignments (Admin)

Admins can manually manage assignments via `POST/PATCH/DELETE /api/rooms/assignments`. Assignments can be for a camper or a leader (camp counselor), not both.

---

## Public Endpoints

No auth required: `/api/auth/**`, `/api/donations/**`, `/api/webhooks/**`, `/actuator/health`, `/v3/api-docs`, `/swagger-ui/**`.

---

## Key Business Rules Summary

1. One active room hold per camper at a time.
2. Room capacity = seats not occupied by (active holds + permanent assignments).
3. A camper cannot be in two active bookings simultaneously.
4. Pricing is locked at booking creation, immune to later tier changes.
5. Members (email/phone match) get `discountPrice`; others get `basePrice`.
6. Stripe webhook processing is idempotent (CAS update, only first succeeds).
7. Booking expiry = 30 min (Stripe session); hold expiry extended to match.
8. First user per organization gets ADMIN role automatically.
9. Soft-deleted tiers (`deletedAt`) are hidden from room/building queries but their prices are preserved in past `BookingItem` records.
10. Donations are anonymous and independent of the booking/camper system.
