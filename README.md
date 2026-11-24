# Task Manager Service

This is a plain Java 17 task-management service. It exposes Java methods instead
of HTTP endpoints, so the same layers can later be placed behind a REST controller.

## Run

```powershell
New-Item -ItemType Directory out
javac -d out (Get-ChildItem -Recurse -Filter *.java)
java -cp out app.Main
java -cp out app.TestRunner
```

The application works offline with the rule-based strategies. For real Anthropic
requests set `LLM_API_KEY`; `LLM_MODEL` is optional. Never commit the key.

## Packages

`model` contains immutable tasks, requests, priorities, and query builders.
`repository` contains the singleton in-memory task and idempotency stores.
`service` contains validation and business rules. `api` converts operations to
`ApiResponse` objects. `parser` contains LLM and rule-based strategies.
`event` contains Observer listeners, `exception` contains the error hierarchy,
and `app` contains runnable demos and offline tests.

## API operations

| Operation | Success | Main errors |
|---|---:|---|
| `createTask(request, key)` | 201 | 400, 422, 500 |
| `getTask(id)` | 200 | 404 |
| `listTasks(query)` | 200 | 500 |
| `updateTask(id, request)` | 200 | 400, 404 |
| `deleteTask(id)` | 200 | 404 |
| `createTaskFromText(text, key)` | 201 | 502, 503, 422 |
| `createSubtask(parentId, request)` | 201 | 400, 404 |
| `listSubtasks(parentId)` | 200 | 404 |
| `breakDownTask(id, key)` | 201 | 400, 404, 422, 502 |
| `listByPriority(level)` | 200 | 400 |

Example:

```java
TaskApi api = new TaskApi(new TaskService(InMemoryTaskRepository.getInstance()));
ApiResponse response = api.createTask(
    new TaskRequest("Pay rent", "Monthly rent", "false", "2026-12-01", "HIGH"),
    "rent-001");
System.out.println(response);
```

See [API_CONTRACT.md](API_CONTRACT.md) for the complete contract.

## Patterns

- **Builder:** `Task` and `TaskQuery`
- **Singleton:** `InMemoryTaskRepository` and `IdempotencyStore`
- **Factory:** `ErrorResponseFactory` and `ParserFactory`
- **Strategy:** `TaskParser` and `BreakdownStrategy`
- **Observer:** `TaskEventListener` and `TaskService`

Task creation prompts include today's date so relative dates can be resolved.
Breakdown prompts require one `title|description` item per line so responses can
be parsed deterministically. All model output is validated before anything is saved.

## Testing

`TestRunner` runs without internet or an API key. It checks validation, CRUD,
idempotency replay and conflict, filtering, breakdown, deletion, and faulty model
output. The hardening cases include impossible dates, blank model titles, unknown
IDs, and repeated keys.