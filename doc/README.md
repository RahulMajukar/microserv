# Documentation

This lab is three Maven modules in one repository. Two of them are services you can later split into their own repositories. The third is a library that is built into both services and is never started by itself.

| Document | What it covers |
| --- | --- |
| [Architecture](architecture.md) | Modules, request path, token sharing, and deploying each service alone |
| [API](api.md) | Every endpoint, the JSON envelope, validation, and error codes |
| [Data and configuration](data.md) | H2, tables, seed rows, and the shared YAML files |
| [Runbook](runbook.md) | Build, start, call the APIs, and reset the in-memory data |

## The shape to copy

```text
caller
  |  POST /api/auth/login
  |  <-- JWT
  |  POST /api/reports/management   Authorization: Bearer <token>
  v
report-service :8082
  |  checks the JWT with the shared secret
  |  JDBC / report-pool --> H2 management_reports
  |  GET /api/revenues/summary
  |  headers: Authorization (same token), X-Request-Id
  v
revenue-service :8081
  |  checks the same JWT with the same secret
  |  JDBC / revenue-pool --> H2 revenues

common-lib is a jar inside both processes. It is not a third server.
```

Java 21 and the Maven Wrapper are required. A database server is not. H2 starts inside each process.
