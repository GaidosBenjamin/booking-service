-- 1. organization (root tenant table)
create table organization
(
    id          uuid primary key default uuidv7(),
    name        text        not null,
    created_on  timestamptz not null,
    created_by  uuid,
    modified_on timestamptz,
    modified_by uuid
);

-- 2. users (authentication and parent/guardian base)
create table users
(
    id            uuid default uuidv7()
        primary key,
    tenant_id     uuid        not null
        references organization (id),
    email         text        not null,
    password_hash text        not null,

    created_on    timestamptz not null,
    created_by    uuid,
    modified_on   timestamptz,
    modified_by   uuid,

    unique (tenant_id, email)
);

-- index to optimize querying by tenant
create index idx_users_tenant on users (tenant_id);

-- 3. user details (1-to-1 extension of users for personal info)
create table user_details
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

create index idx_user_details_tenant on user_details (tenant_id);

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