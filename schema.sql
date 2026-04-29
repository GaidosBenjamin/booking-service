-- 1. organization (root tenant table)
create table organization
(
    id          uuid primary key default uuidv7(),
    slug        text        not null,
    name        text        not null,
    created_on  timestamptz not null,
    created_by  uuid,
    modified_on timestamptz,
    modified_by uuid,

    unique (slug)
);

-- 2. users (authentication and parent/guardian base)
create table users
(
    id             uuid                 default uuidv7()
        primary key,
    tenant_id      uuid        not null
        references organization (id),
    email          text        not null,
    password_hash  text        not null,
    email_verified boolean     not null default false,

    created_on     timestamptz not null,
    created_by     uuid,
    modified_on    timestamptz,
    modified_by    uuid,

    unique (tenant_id, email)
);

-- index to optimize querying by tenant
create index idx_users_tenant on users (tenant_id);

-- 3. user details (1-to-1 extension of users for personal info)
create table user_profile
(
    id          uuid default uuidv7()
        primary key,
    user_id     uuid        not null
        references users (id) on delete cascade,
    tenant_id   uuid        not null
        references organization (id),

    first_name  text        not null,
    last_name   text        not null,
    phone       text        not null,
    address     text,

    created_on  timestamptz not null,
    created_by  uuid,
    modified_on timestamptz,
    modified_by uuid,

    unique (user_id)
);

create index idx_user_profile_tenant on user_profile (tenant_id);

-- 4. roles (named collection of permissions, tenant-scoped)
create table roles
(
    id          uuid                 default uuidv7()
        primary key,
    tenant_id   uuid        not null
        references organization (id),

    name        text        not null,
    permissions text[]      not null default '{}',

    created_on  timestamptz not null,
    created_by  uuid,
    modified_on timestamptz,
    modified_by uuid,

    unique (tenant_id, name)
);

create index idx_roles_tenant on roles (tenant_id);

-- 5. user_roles (assigns roles to users)
create table user_roles
(
    id          uuid default uuidv7()
        primary key,
    tenant_id   uuid        not null
        references organization (id),
    user_id     uuid        not null
        references users (id) on delete cascade,
    role_id     uuid        not null
        references roles (id) on delete cascade,

    created_on  timestamptz not null,
    created_by  uuid,
    modified_on timestamptz,
    modified_by uuid,

    unique (user_id, role_id)
);

create index idx_user_roles_tenant on user_roles (tenant_id);
create index idx_user_roles_user on user_roles (user_id);

-- 6. password_reset_tokens
create table password_reset_tokens
(
    id          uuid primary key default uuidv7(),
    tenant_id   uuid        not null
        references organization (id),
    user_id     uuid        not null
        references users (id) on delete cascade,

    token_hash  text        not null,
    expires_at  timestamptz not null,
    consumed_at timestamptz,

    created_on  timestamptz not null,
    created_by  uuid
);

create index idx_prt_token_hash on password_reset_tokens (token_hash);
create index idx_prt_user on password_reset_tokens (user_id);

-- 7. email_verification_tokens
create table email_verification_tokens
(
    id          uuid primary key default uuidv7(),
    tenant_id   uuid        not null
        references organization (id),
    user_id     uuid        not null
        references users (id) on delete cascade,

    token_hash  text        not null,
    expires_at  timestamptz not null,
    consumed_at timestamptz,

    created_on  timestamptz not null,
    created_by  uuid
);

create index idx_evt_token_hash on email_verification_tokens (token_hash);
create index idx_evt_user on email_verification_tokens (user_id);

-- 8. refresh_tokens
create table refresh_tokens
(
    id         uuid primary key default uuidv7(),
    tenant_id  uuid        not null
        references organization (id),
    user_id    uuid        not null
        references users (id) on delete cascade,
    family_id  uuid        not null,

    token_hash text        not null,
    expires_at timestamptz not null,
    revoked_at timestamptz,

    created_on timestamptz not null,
    created_by uuid
);

create unique index idx_rt_token_hash on refresh_tokens (token_hash);
create index idx_rt_user on refresh_tokens (user_id);
create index idx_rt_family on refresh_tokens (family_id);

-- 9. campers
create type camper_status as enum ('NEEDS_BED', 'NEEDS_PAYMENT', 'PAYMENT_SUCCESS', 'PAYMENT_FAILED');

create table campers
(
    id                   uuid primary key       default uuidv7(),
    tenant_id            uuid          not null
        references organization (id),
    parent_user_id       uuid          not null
        references users (id),

    first_name           text          not null,
    last_name            text          not null,
    date_of_birth        date          not null,
    grade                text          not null,
    gender               text          not null check (gender in ('male', 'female')),
    special_requirements text,

    status               camper_status not null default 'NEEDS_BED',

    created_on           timestamptz   not null,
    created_by           uuid,
    modified_on          timestamptz,
    modified_by          uuid
);

create index idx_campers_parent on campers (parent_user_id);
create index idx_campers_tenant on campers (tenant_id);

-- 10. tiers
create table tiers
(
    id             uuid                    default uuidv7()
        primary key,
    tenant_id      uuid           not null
        references organization (id),
    name           text           not null,
    description    text,
    base_price     decimal(10, 2) not null,
    discount_price decimal(10, 2) not null,
    currency       char(3)        not null default 'RON',
    deleted_at     timestamptz,

    created_on     timestamptz    not null,
    created_by     uuid,
    modified_on    timestamptz,
    modified_by    uuid,

    constraint tiers_prices_valid
        check (base_price >= 0
            and discount_price >= 0
            and discount_price <= base_price)
);

create index idx_tiers_tenant on tiers (tenant_id);
create index idx_tiers_active on tiers (tenant_id) where deleted_at is null;

-- 11. buildings
create table buildings
(
    id          uuid   default uuidv7()
        primary key,
    tenant_id   uuid                not null
        references organization (id),
    tier_id     uuid                not null
        references tiers (id),
    name        text                not null,
    description text,
    highlights  text[] default '{}' not null,
    image_url   text                not null,

    created_on  timestamptz         not null,
    created_by  uuid                not null,
    modified_on timestamptz,
    modified_by uuid
);

create index idx_buildings_tenant on buildings (tenant_id);

-- 12. leaders
create table leaders
(
    id            uuid default uuidv7()
        primary key,
    tenant_id     uuid        not null
        references organization (id),

    first_name    text        not null,
    last_name     text        not null,
    date_of_birth date check (date_of_birth < current_date),
    gender        text check (gender in ('male', 'female')),

    created_on    timestamptz not null,
    created_by    uuid        not null,
    modified_on   timestamptz,
    modified_by   uuid
);

create index idx_leaders_tenant on leaders (tenant_id);

-- 13. rooms
create table rooms
(
    id             uuid    default uuidv7()
        primary key,
    tenant_id      uuid        not null
        references organization (id),
    building_id    uuid        not null
        references buildings (id),
    name           text        not null,
    capacity       int         not null,
    image_url      text        not null,

    allowed_gender text check (allowed_gender in ('male', 'female')),
    min_age        int check (min_age >= 0),
    max_age        int check (max_age >= 0),
    leader_room    boolean default false,

    created_on     timestamptz not null,
    created_by     uuid        not null,
    modified_on    timestamptz,
    modified_by    uuid,

    constraint chk_age_range check (max_age >= min_age)
);

create index idx_rooms_tenant on rooms (tenant_id);
create index idx_rooms_building on rooms (building_id);

-- 14. room_holds
create table room_holds
(
    id         uuid primary key default uuidv7(),
    tenant_id  uuid        not null references organization (id),
    room_id    uuid        not null references rooms (id) on delete cascade,
    camper_id  uuid        not null references campers (id) on delete cascade,

    expires_at timestamptz not null,

    unique (camper_id)
);

create index idx_room_holds_room_expires on room_holds (room_id, expires_at);

-- 15. room_assignments
create table room_assignments
(
    id          uuid primary key default uuidv7(),
    tenant_id   uuid        not null
        references organization (id),
    room_id     uuid        not null
        references rooms (id) on delete cascade,

    camper_id   uuid references campers (id) on delete cascade,
    leader_id   uuid references leaders (id) on delete cascade,

    assigned_on timestamptz      default current_timestamp,

    created_on  timestamptz not null,
    created_by  uuid,
    modified_on timestamptz,
    modified_by uuid,

    constraint chk_single_occupant check (
        (camper_id is not null and leader_id is null) or
        (camper_id is null and leader_id is not null)
        ),

    unique (camper_id),
    unique (leader_id)
);

create index idx_room_assignments_room on room_assignments (room_id);

-- 16. code_of_conduct
create table code_of_conduct
(
    id          uuid primary key default uuidv7(),
    tenant_id   uuid        not null references organization (id),

    content     jsonb       not null,
    is_active   boolean          default true,

    created_on  timestamptz not null,
    created_by  uuid        not null,
    modified_on timestamptz,
    modified_by uuid
);

create index idx_code_of_conduct_tenant on code_of_conduct (tenant_id);

-- 17. code_of_conduct_agreements
create table code_of_conduct_agreements
(
    id                 uuid        default uuidv7()
        primary key,
    tenant_id          uuid not null
        references organization (id),
    code_of_conduct_id uuid not null
        references code_of_conduct (id),
    user_id            uuid not null
        references users (id),

    agreed_on          timestamptz default current_timestamp,

    unique (user_id, code_of_conduct_id)
);

create index idx_code_of_conduct_agreements_tenant on code_of_conduct_agreements (tenant_id);
create index idx_code_of_conduct_agreements_user on code_of_conduct_agreements (user_id);

-- 18. payment_status enum + bookings + booking_items
create type payment_status as enum ('PENDING', 'SUCCEEDED', 'FAILED', 'CANCELED');

create table bookings
(
    id                uuid primary key        default uuidv7(),
    tenant_id         uuid           not null references organization (id),
    parent_user_id    uuid           not null references users (id),

    stripe_session_id text           not null,
    amount_total      decimal(10, 2) not null,
    currency          char(3)        not null default 'RON',
    status            payment_status not null default 'PENDING',

    expires_at        timestamptz,

    created_on        timestamptz    not null,
    created_by        uuid           not null,
    modified_on       timestamptz,
    modified_by       uuid,

    unique (stripe_session_id)
);

create index idx_bookings_tenant on bookings (tenant_id);
create index idx_bookings_parent on bookings (parent_user_id);
create index idx_bookings_session on bookings (stripe_session_id);

create table booking_items
(
    id          uuid primary key default uuidv7(),
    tenant_id   uuid           not null references organization (id),
    booking_id  uuid           not null references bookings (id) on delete cascade,

    camper_id   uuid           not null references campers (id),
    tier_id     uuid           not null references tiers (id),
    room_id     uuid           not null references rooms (id),

    price       decimal(10, 2) not null,

    created_on  timestamptz    not null,
    created_by  uuid           not null,
    modified_on timestamptz,
    modified_by uuid,

    unique (booking_id, camper_id)
);

create index idx_booking_items_tenant on booking_items (tenant_id);
create index idx_booking_items_booking on booking_items (booking_id);

-- 19. members
create table members
(
    id        uuid primary key default uuidv7(),
    tenant_id uuid null references organization (id),
    name      text not null,
    email     text null,
    phone     text null
);

create index idx_members_tenant on members (tenant_id);

alter table "booking-service".buildings
    drop column description;

alter table "booking-service".buildings
    drop column highlights;

alter table "booking-service".buildings
    add description jsonb;

alter table "booking-service".buildings
    add highlights jsonb;

alter table room_assignments
    alter column created_by drop not null;

-- 20. donations
create table donations
(
    id                uuid primary key default uuidv7(),
    name              varchar(255),
    org_slug          varchar(255)   not null,
    amount            numeric(12, 2) not null,
    currency          varchar(3)     not null,
    status            payment_status not null,
    stripe_session_id varchar(255)   not null unique,
    expires_at        timestamptz,
    created_on        timestamptz    not null,
    created_by        uuid,
    modified_on       timestamptz,
    modified_by       uuid
);