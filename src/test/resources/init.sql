create table if not exists test.idempotency
(
  id varchar(32) not null,
  processed boolean,
  timestamp timestamp not null default now(),
  data jsonb null,
  constraint idempotency_pkey primary key (id)
);