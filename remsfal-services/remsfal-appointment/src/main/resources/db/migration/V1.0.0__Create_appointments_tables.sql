-- Create appointments table
create table appointments (
    id uuid not null,
    cancellation_reason varchar(500),
    confirmed_end timestamp(6),
    confirmed_start timestamp(6),
    craftsman_id varchar(255) not null,
    created_at timestamp(6) not null,
    duration_minutes integer not null,
    from_time timestamp(6) not null,
    resource_id varchar(255) not null,
    status varchar(255) not null check ((status in ('OPEN','CONFIRMED','DECLINED','CANCELLED'))),
    timezone varchar(255) not null,
    to_time timestamp(6) not null,
    type varchar(255) not null check ((type in ('VIEWING','REPAIR'))),
    updated_at timestamp(6),
    primary key (id)
);

-- Create breaks table
create table breaks (
    id uuid not null,
    end_time time(0) not null,
    start_time time(0) not null,
    working_hours_id uuid not null,
    primary key (id)
);

-- Create working_hours table
create table working_hours (
    id uuid not null,
    end_time time(0) not null,
    start_time time(0) not null,
    appointment_id uuid not null,
    primary key (id)
);

-- Add unique constraint
alter table if exists working_hours 
   drop constraint if exists UKtdcd6ewfibgqmdiwckxdsds9f;

alter table if exists working_hours 
   add constraint UKtdcd6ewfibgqmdiwckxdsds9f unique (appointment_id);

-- Add foreign key constraints
alter table if exists breaks 
   add constraint FKalhier17uk75jjhsp0wduvegg 
   foreign key (working_hours_id) 
   references working_hours;

alter table if exists working_hours 
   add constraint FK87och5ql9phfqft080v0fpqyw 
   foreign key (appointment_id) 
   references appointments;
