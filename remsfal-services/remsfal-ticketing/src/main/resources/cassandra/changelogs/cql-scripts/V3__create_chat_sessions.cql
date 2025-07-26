CREATE TABLE IF NOT EXISTS REMSFAL.chat_sessions (
                                                     project_id UUID,                -- Partition key
                                                     task_id UUID,                   -- Partition key
                                                     session_id UUID,                -- Clustering column
                                                     participants MAP<UUID, TEXT>,   -- Participant ID -> Role
                                                     created_at TIMESTAMP,
                                                     modified_at TIMESTAMP,
                                                     PRIMARY KEY ((project_id, task_id), session_id)  -- Composite primary key
) WITH CLUSTERING ORDER BY (session_id ASC);  -- Clustering order
