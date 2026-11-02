# API

Both services return the same envelope. `responseCode` matches the HTTP status.

```json
{
  "responseCode": 200,
  "responseMessage": "SUCCESS",
  "responseBody": {}
}
```

| `responseCode` | `responseMessage` | When |
| --- | --- | --- |
| 200 | `SUCCESS` | Read succeeded |
| 201 | `SUCCESS` | A revenue row or a report was created |
| 400 | `Validation failed` | Bean validation failed. `responseBody` is a map of field name to message |
| 400 | specific text | Bad range, missing query parameter, bad type, or a body that is not JSON |
| 401 | `Missing or invalid token` | `/api` call without a valid `Authorization: Bearer` token |
| 401 | `Invalid username or password` | Login rejected |
| 404 | `Revenue {id} not found` or `Management report {id} not found` | Unknown id |
| 500 | `Unexpected error` | Unhandled failure. The stack trace is only in the server log |
| 502 | downstream message | Report called revenue and that call failed. No report row is inserted |

Login and health do not require a token. Send the token on every other `/api` call:

```http
Authorization: Bearer <token>
```

Optional on any call. If you omit it, the service creates one and returns it:

```http
X-Request-Id: learn-demo-1
```

PowerShell removes quotes before they reach `curl.exe`. Use `Invoke-RestMethod` for JSON bodies. GET calls can use `curl.exe`.

## Login

`POST /api/auth/login` on port 8081 or 8082.

```json
{ "username": "learner", "password": "learner" }
```

```json
{
  "responseCode": 200,
  "responseMessage": "SUCCESS",
  "responseBody": {
    "token": "<jwt>",
    "tokenType": "Bearer",
    "expiresInMinutes": 60
  }
}
```

A blank username or password is **400** with message `username and password are required`. A wrong pair is **401**.

## Revenue service

Base URL `http://localhost:8081`.

### Create a revenue row

`POST /api/revenues`

```json
{
  "source": "store",
  "category": "ads",
  "amount": 25.50,
  "currency": "usd",
  "recordedOn": "2026-03-04",
  "note": "counter"
}
```

| Field | Rule |
| --- | --- |
| `source` | Required, max 100 characters |
| `category` | Required, max 100 characters |
| `amount` | Required, at least `0.01` |
| `currency` | Required, exactly 3 characters. Stored in upper case |
| `recordedOn` | Required date |
| `note` | Optional, max 255 characters. Blank becomes null |

The amount is stored with scale 2, half up. Response **201** and a `RevenueResponse`:

```json
{
  "id": 5,
  "source": "store",
  "category": "ads",
  "amount": 25.50,
  "currency": "USD",
  "recordedOn": "2026-03-04",
  "note": "counter"
}
```

### List revenues

`GET /api/revenues`

`GET /api/revenues?from=2026-01-01&to=2026-01-31`

With no dates, every row is returned, newest `recordedOn` first. With both dates, only that inclusive range is returned, oldest first. One date without the other is **400**: `from and to must be provided together`. A reversed range is **400**: `from must be on or before to`.

### One revenue row

`GET /api/revenues/{id}`

Unknown id is **404**.

### Summary

`GET /api/revenues/summary?from=2026-01-01&to=2026-01-31`

Both query parameters are required. The body is a `RevenueSummary`:

```json
{
  "responseCode": 200,
  "responseMessage": "SUCCESS",
  "responseBody": {
    "from": "2026-01-01",
    "to": "2026-01-31",
    "totalAmount": 1500.00,
    "currency": "USD",
    "byCategory": [
      { "category": "ads", "amount": 300.00 },
      { "category": "subscriptions", "amount": 1200.00 }
    ]
  }
}
```

`currency` is `USD` when the range has no rows, the single currency code when every row uses that code, and `MIXED` when more than one currency is present.

The January seed total is `1500.00 USD`. February 1 through 28 is `2300.00`. January 1 through February 28 is `3800.00`.

## Report service

Base URL `http://localhost:8082`.

### Generate a report

`POST /api/reports/management`

```json
{ "from": "2026-01-01", "to": "2026-01-31" }
```

Both dates are required. The service calls the revenue summary, then stores a snapshot. Response **201**:

```json
{
  "id": 3,
  "periodFrom": "2026-01-01",
  "periodTo": "2026-01-31",
  "totalRevenue": 1500.00,
  "currency": "USD",
  "byCategory": [
    { "category": "ads", "amount": 300.00 },
    { "category": "subscriptions", "amount": 1200.00 }
  ],
  "narrative": "Management report 2026-01-01 to 2026-01-31: total 1500.00 USD. ads 300.00, subscriptions 1200.00.",
  "createdAt": "2026-11-02T04:10:00"
}
```

Calling this twice stores two rows with the same totals. Each row is a new snapshot.

### List reports

`GET /api/reports`

Newest `createdAt` first. After a fresh start this returns the two seeded reports: January `1500.00` and February `2300.00`.

### One report

`GET /api/reports/{id}`

Unknown id is **404**.

## Health

`GET /actuator/health` on either port. No token. A running service returns HTTP 200.

## H2 console

`http://localhost:8081/h2-console` shows the revenue process. `http://localhost:8082/h2-console` shows the report process.

| Field | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:microserv` |
| User | `sa` |
| Password | empty |

The URL must match exactly. A different URL opens a different empty database. The console only sees the database inside that process.
