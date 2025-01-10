CREATE TABLE IF NOT EXISTS REMSFAL.chat_sessions (
                                             project_id UUID,                -- Partition key
                                             session_id UUID,                -- Clustering column
                                             task_id UUID,
                                             task_type TEXT,
                                             participants MAP<TEXT, TEXT>,   -- Participant ID -> Role
                                             status TEXT,
                                             created_at TIMESTAMP,
                                             modified_at TIMESTAMP,
                                             PRIMARY KEY (project_id, session_id)  -- Composite primary key
) WITH CLUSTERING ORDER BY (session_id ASC);
