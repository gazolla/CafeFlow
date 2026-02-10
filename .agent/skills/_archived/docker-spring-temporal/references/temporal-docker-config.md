# Temporal Docker Configuration Reference

When running Temporal in Docker, especially with the `auto-setup` image, there are several key configurations.

## Auto-Setup Image
The `temporalio/auto-setup` image is designed for development. It automatically:
1.  Waits for the database to be ready.
2.  Creates the `temporal` and `temporal_visibility` databases if they don't exist.
3.  Runs database migrations (schema setup).
4.  Starts the Temporal Server services (Frontend, History, Matching, Worker).

## Key Environment Variables

| Variable | Description |
| :--- | :--- |
| `DB` | The database type (`postgresql`, `mysql`, `cassandra`). |
| `POSTGRES_SEEDS` | The hostname of the database container. |
| `DB_PORT` | The port for the database (default 5432 as of Postgres). |
| `DYNAMIC_CONFIG_FILE_PATH` | Path to a YAML file for advanced Temporal configuration. |

## Persistence
Always mount volumes for your database. If the database container is deleted without a volume, you will lose your Temporal history and workflow states.

```yaml
volumes:
  postgres_data:
```

## Troubleshooting
- **Connection Refused**: Ensure the app is in the same Docker network as the Temporal container.
- **DB Start Failure**: If using Postgres 15+, ensure the user has permissions to create the database or manually create the `temporal` database.
