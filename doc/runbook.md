# Runbook

From the project root. Java 21 is required. Maven Wrapper is included, so a global Maven install is not required. No database server is required.

## Test and build

```powershell
.\mvnw.cmd test
.\mvnw.cmd -pl revenue-service -am package
.\mvnw.cmd -pl report-service -am package
```

`-am` builds `common-lib` before the service. The runnable files are:

- `revenue-service\target\revenue-service-1.0.0.jar`
- `report-service\target\report-service-1.0.0.jar`

Start each jar in its own terminal:

```powershell
java -jar revenue-service\target\revenue-service-1.0.0.jar
java -jar report-service\target\report-service-1.0.0.jar
```

Or let Maven start them, still from the project root, one terminal each:

```powershell
.\mvnw.cmd -pl revenue-service -am spring-boot:run
.\mvnw.cmd -pl report-service -am spring-boot:run
```

Wait until both logs say the application started.

## Call the APIs

```powershell
curl.exe -s http://localhost:8081/actuator/health
curl.exe -s http://localhost:8082/actuator/health

$login = Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/auth/login -ContentType "application/json" -Body '{"username":"learner","password":"learner"}'
$token = $login.responseBody.token

curl.exe -s -H "Authorization: Bearer $token" http://localhost:8081/api/revenues
curl.exe -s -H "Authorization: Bearer $token" "http://localhost:8081/api/revenues/summary?from=2026-01-01&to=2026-01-31"
curl.exe -s -H "Authorization: Bearer $token" http://localhost:8082/api/reports

Invoke-RestMethod -Method Post -Uri http://localhost:8082/api/reports/management -ContentType "application/json" -Headers @{ Authorization = "Bearer $token"; "X-Request-Id" = "learn-demo-1" } -Body '{"from":"2026-01-01","to":"2026-01-31"}'
```

A fresh start already returns four revenue rows and two reports. The January summary is `1500.00 USD`. The new report is **201**. Both consoles show `[learn-demo-1]` for that report call. The report log says it is forwarding the caller token. The revenue log says `Authenticated user learner`.

## Checks worth making

1. Call the January summary with no `Authorization` header. The response is **401** and `Missing or invalid token`.
2. Generate the January report twice. `GET /api/reports` shows two extra snapshots with the same total. The seeded rows stay.
3. Add a March revenue row, then generate a March report. The old January report stays `1500.00`.

   ```powershell
   Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/revenues -ContentType "application/json" -Headers @{ Authorization = "Bearer $token" } -Body '{"source":"store","category":"ads","amount":25.50,"currency":"usd","recordedOn":"2026-03-04","note":"counter"}'
   ```

4. Stop the revenue terminal and generate a report again. The result is **502**, and no new report row is stored.
5. Call summary with `from=2026-02-01&to=2026-01-01`. The result is **400**. The revenue log shows the request id, and the SQL does not run.
6. Omit `amount` from a create body. The result is **400**, `Validation failed`, and `responseBody.amount` explains the field.

## Reset

Stop the service and start it again. H2 is created empty, `schema.sql` builds the tables, and that service's `data.sql` loads the sample rows. Rows added through the API are gone after the restart. Restarting one service does not clear the other service's database.

## Split onto two machines

1. Build both jars.
2. Copy `revenue-service-1.0.0.jar` to host A and start it.
3. Copy `report-service-1.0.0.jar` to host B.
4. Set `clients.revenue.base-url` to `http://<host-a>:8081` before building the report jar, or override that property at startup:

   ```powershell
   java -jar report-service-1.0.0.jar --clients.revenue.base-url=http://<host-a>:8081
   ```

5. Give both hosts the same `auth.jwt.secret`. A token from one host is rejected by the other when the secrets differ.

Each host still has a private H2 database. Report never opens a connection to the revenue database.
