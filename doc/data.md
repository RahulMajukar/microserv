# Data and configuration

Each process opens its own H2 database when it starts. The URL is the same string in both services, and it is still two databases, because H2 memory belongs to the JVM.

```text
jdbc:h2:mem:microserv;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE
```

User `sa`, empty password. Pool size 5. Revenue names its pool `revenue-pool`. Report names its pool `report-pool`.

Stopping a process drops its rows. The next start runs `schema.sql`, then that service's `data.sql`.

## Where the files live

| File | Loaded by | Role |
| --- | --- | --- |
| `common-lib/src/main/resources/application-datasource.yml` | both | H2 URL, user, SQL init |
| `common-lib/src/main/resources/application-auth.yml` | both | JWT secret, token lifetime, lab user |
| `common-lib/src/main/resources/schema.sql` | both | `CREATE TABLE IF NOT EXISTS` for both tables |
| `revenue-service/src/main/resources/data.sql` | revenue only | Four sample revenue rows |
| `report-service/src/main/resources/data.sql` | report only | Two sample reports |
| `revenue-service/src/main/resources/application.yml` | revenue | Port 8081 and pool name |
| `report-service/src/main/resources/application.yml` | report | Port 8082, pool name, `clients.revenue.base-url` |

Do not name a shared file `application.yml`. Both services import the shared files with `spring.config.import`.

## Tables

`revenues`

| Column | Type | Rule |
| --- | --- | --- |
| `id` | identity | Generated on insert |
| `source` | varchar(100) | Required |
| `category` | varchar(100) | Required |
| `amount` | numeric(14,2) | Required, must be greater than 0 |
| `currency` | varchar(3) | Required |
| `recorded_on` | date | Required |
| `note` | varchar(255) | Optional |

`management_reports`

| Column | Type | Rule |
| --- | --- | --- |
| `id` | identity | Generated on insert |
| `period_from`, `period_to` | date | Required |
| `total_revenue` | numeric(14,2) | Required |
| `currency` | varchar(10) | Required |
| `breakdown_json` | varchar(2000) | JSON array of `{ "category", "amount" }` |
| `narrative` | varchar(2000) | Required |
| `created_at` | timestamp | Required |

Report does not query `revenues`. The `revenues` table also exists in the report process because both processes run the same `schema.sql`. Report never reads it. Its numbers come from the revenue HTTP API, then from its own snapshot.

## Seed rows

Revenue, loaded only into the revenue process:

| source | category | amount | currency | recorded_on | note |
| --- | --- | --- | --- | --- | --- |
| web-app | subscriptions | 1200.00 | USD | 2026-01-15 | January plans |
| web-app | ads | 300.00 | USD | 2026-01-20 | January ads |
| web-app | subscriptions | 1500.00 | USD | 2026-02-15 | February plans |
| consulting | services | 800.00 | USD | 2026-02-18 | Workshop |

Report, loaded only into the report process:

| period | total | categories |
| --- | --- | --- |
| 2026-01-01 to 2026-01-31 | 1500.00 USD | ads 300.00, subscriptions 1200.00 |
| 2026-02-01 to 2026-02-28 | 2300.00 USD | services 800.00, subscriptions 1500.00 |

## SQL in the services

`RevenueDao` writes the revenue statements: insert, find by id, find all, find between dates, sum by category, and distinct currencies. `RevenueRowMapper` reads one revenue row. `CategoryAmountRowMapper` reads one grouped category total.

`ManagementReportDao` inserts a report and reads reports by id or all rows, newest first. `ManagementReportRowMapper` parses `breakdown_json` back into `List<CategoryAmount>`.

Inserts use a generated key named `id`.

## Auth configuration

```yaml
auth:
  jwt:
    secret: learning-lab-jwt-secret-change-me-32b
    ttl-minutes: 60
  user:
    username: learner
    password: learner
```

The secret is a lab value. In a real deployment both hosts must receive the same secret from outside the jar, and the password must not stay in source control. The code path does not change: each process still verifies the signature locally.
