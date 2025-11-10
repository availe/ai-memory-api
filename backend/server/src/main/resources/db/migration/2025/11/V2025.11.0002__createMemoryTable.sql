create table memories
(
    id          uuid primary key     default uuidv7(),
    content     text        not null,
    embedding   vector(768) not null,
    source_type text        not null,
    metadata    jsonb       not null default '{}'::jsonb,
    created_at  timestamptz not null default now()
);

create index on memories using hnsw (embedding vector_cosine_ops);

create table memory_edges
(
    source_id    uuid        not null references memories (id) on delete cascade,
    target_id    uuid        not null references memories (id) on delete cascade,
    relationship text        not null,
    created_at   timestamptz not null default now(),
    primary key (source_id, target_id, relationship)
);

create index on memory_edges (source_id);
create index on memory_edges (target_id);