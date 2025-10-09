create table if not exists users (
  id           bigserial primary key,
  email        varchar(320) not null unique,
  password_hash varchar(100) not null,
  created_at   timestamp with time zone default now()
);
