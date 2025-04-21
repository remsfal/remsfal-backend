CREATE KEYSPACE IF NOT EXISTS REMSFAL
    WITH replication = {
        'class': 'NetworkTopologyStrategy',
        'datacenter1': 2
        };
