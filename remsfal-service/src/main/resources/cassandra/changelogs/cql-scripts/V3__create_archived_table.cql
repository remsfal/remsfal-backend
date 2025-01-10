-- Script: V3__create_archived_table.cql
CREATE TABLE IF NOT EXISTS REMSFAL.archived_chat_messages (
                                                              chat_session_id UUID,
                                                              message_id UUID,
                                                              sender_id UUID,
                                                              content_type TEXT,
                                                              content TEXT,
                                                              url TEXT,
                                                              created_at TIMESTAMP,
                                                              PRIMARY KEY (chat_session_id, message_id)
) WITH CLUSTERING ORDER BY (message_id ASC);
