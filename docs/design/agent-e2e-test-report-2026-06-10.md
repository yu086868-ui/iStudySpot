# AI Agent Test Report

Date: 2026-06-10

## Scope

This report records the current verification status of the iStudySpot AI Agent flow after the recent backend and Android integration work.

Covered areas:

- Backend agent controller and service tests
- Android Agent ViewModel tests
- Docker-based backend runtime validation
- Public HTTPS end-to-end validation through `https://frp-six.com:37379`

## Environment

- Workspace: `F:\Scylier-Project\active\iStudySpot`
- Backend runtime: `docker compose`
- Public endpoint: `https://frp-six.com:37379`
- Android build target: debug
- Test date timezone: Asia/Shanghai

## Automated Test Results

### Backend targeted tests

Command:

```powershell
.\mvnw.cmd -q "-Dtest=AgentChatControllerTest,AgentToolControllerTest,AgentChatServiceImplTest,AgentToolServiceImplTest,ReservationRulesProviderTest" test
```

Result:

- Passed

Coverage focus:

- Agent chat success and structured error payloads
- Agent tool success and unauthorized payload behavior
- Reservation rules loaded from the shared rules provider
- Redacted reservation output with placeholder references such as `ORDER_REF_1`

### Android targeted tests

Command:

```powershell
.\gradlew.bat --stop
.\gradlew.bat --offline --no-daemon --no-parallel "-Dkotlin.incremental=false" :app:testDebugUnitTest --tests com.example.scylier.istudyspot.AgentViewModelTest
```

Result:

- Passed

Coverage focus:

- Catalog loading
- Chat success handling
- Fallback suggestions when backend suggestions are empty
- Unauthorized user message handling
- Tool shortcut behavior for parameterized tools
- Suggestion generation using returned room ids instead of a hard-coded fallback

### Android compile verification

Command:

```powershell
.\gradlew.bat --offline --no-daemon --no-parallel "-Dkotlin.incremental=false" :app:compileDebugKotlin
```

Result:

- Passed

Additional verification:

- Generated debug `BuildConfig.BASE_URL` points to `https://frp-six.com:37379/`

## Docker Runtime Validation

Commands:

```powershell
docker compose build backend
docker compose up -d backend
docker compose ps
```

Result:

- Passed

Observed runtime state:

- `mysql`: healthy
- `redis`: healthy
- `backend`: healthy
- `admin`: running

## Public HTTPS End-to-End Validation

The following sequence was executed against the public HTTPS endpoint:

1. Register a new user
2. Login and obtain JWT
3. Access `/api/agent/tools/catalog` without JWT
4. Access `/api/agent/tools/catalog` with JWT
5. Run `/api/agent/chat` for reservation rules
6. Run `/api/agent/chat` for study room listing
7. Run `/api/agent/chat` for seat listing using a returned room id
8. Run `/api/agent/chat` for current user reservations
9. Run `/api/agent/tools/execute` for `get_reservation_rules`
10. Run `/api/reservations/rules`
11. Compare rule values returned by the tool endpoint and reservation rules endpoint

### E2E results

- Register: `200`
- Login: `200`
- Unauthenticated catalog access: `401`
- Authenticated catalog access: `200`
- Catalog tool count: `5`
- Rules chat: `200`
- Rules chat reply: `I found the reservation rules. You can reserve up to 7 days in advance.`
- Study room chat: `200`
- Study room tool selected: `list_study_rooms`
- Study room count returned in this run: `3`
- First room id used for seat query: `1`
- Seat chat: `200`
- Seat tool selected: `list_room_seats`
- Seat count returned in this run: `11`
- Reservation chat: `200`
- Reservation tool selected: `get_my_reservations`
- Reservation item count for the fresh test user: `0`
- Rules tool execution: `200`
- Rules tool `maxAdvanceDays`: `7`
- Reservation rules endpoint: `200`
- Reservation rules endpoint `maxAdvanceDays`: `7`
- Shared rule consistency check: `true`

## Business Flow Validation Before Agent Query

To verify the agent against real business data rather than only rule lookup or empty-user scenarios, an additional flow was executed through the public HTTPS endpoint:

1. Register a fresh user
2. Login and obtain JWT
3. Query study rooms
4. Query seats for the selected study room
5. Create a reservation through the normal reservation API
6. Query `/api/reservations/my`
7. Query the AI Agent for `Show my reservations`
8. Query the AI Agent for `Show seats for room {id}`

### Business flow results

- Register: `200`
- Login: `200`
- Selected study room id: `1`
- Selected seat id: `2`
- Selected seat status before booking: `available`
- Reservation create: `200`
- Reservation create message: success
- `/api/reservations/my` count after booking: `1`
- Agent reservation query code: `200`
- Agent reservation tool selected: `get_my_reservations`
- Agent reservation item count: `1`
- Agent seat query code: `200`
- Agent seat query tool selected: `list_room_seats`
- Agent seat item count in this run: `11`

### Interpretation

- The standard reservation business flow is usable through the current public backend.
- The AI Agent can read and summarize the newly created reservation after it is created through the normal reservation API.
- This confirms that the agent is not limited to empty-user test data and can operate on real post-booking state.

### Unauthorized payload example

Observed payload for unauthenticated access to `/api/agent/tools/catalog`:

```json
{
  "code": 401,
  "message": "UNAUTHORIZED",
  "data": {
    "schemaVersion": "1.0",
    "error": {
      "code": "UNAUTHORIZED",
      "retryable": false
    }
  }
}
```

## Key Findings

- The backend agent flow is operational through Docker and through the public HTTPS tunnel.
- JWT protection works for agent endpoints and returns a structured machine-readable payload.
- The Android client is configured to use the public HTTPS endpoint.
- The Android agent shortcut behavior is improved: tools that need context no longer blindly fail from the entry screen.
- Reservation rules are now served from the shared rules source and stay consistent between:
  - `/api/agent/tools/execute`
  - `/api/reservations/rules`
- Fresh users correctly receive an empty reservation result instead of an error when no reservations exist.

## Remaining Risks

- The FRP HTTPS certificate still requires permissive handling in local debug verification environments; this is acceptable for debug validation but should not be mirrored into release trust behavior.
- The agent currently uses deterministic routing rules and tool selection rather than a more advanced planner. This is stable, but still intentionally narrow.
- UI action routing still relies on string route names. A typed route mapping layer would reduce future regression risk.

## Suggested Next Steps

- Add typed backend-to-frontend action mapping for agent navigation.
- Add stricter tool argument schema validation and response contract tests.
- Add one or two Dockerized API smoke tests that can be run automatically in CI.
