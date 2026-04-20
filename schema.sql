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
    id             uuid default uuidv7()
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
    id          uuid default uuidv7()
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
    id          uuid primary key default uuidv7(),
    tenant_id   uuid        not null
        references organization (id),
    user_id     uuid        not null
        references users (id) on delete cascade,
    family_id   uuid        not null,

    token_hash  text        not null,
    expires_at  timestamptz not null,
    revoked_at  timestamptz,

    created_on  timestamptz not null,
    created_by  uuid
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
    gender               text          not null,
    special_requirements text,

    status               camper_status not null default 'NEEDS_BED',

    created_on           timestamptz   not null,
    created_by           uuid,
    modified_on          timestamptz,
    modified_by          uuid
);

create index idx_campers_parent on campers (parent_user_id);
create index idx_campers_tenant on campers (tenant_id);