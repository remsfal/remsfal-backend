-- Script: V1__create_keyspace.cql
CREATE KEYSPACE IF NOT EXISTS REMSFAL
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};
