# API Contract

Every method returns `ApiResponse(status, body)`. Success bodies contain a task,
a list of tasks, or a confirmation object. All failures use this shape:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [{"field": "title", "issue": "must not be empty"}]
  }
}
```

## Operations

| Method | Parameters | Success | Errors |
|---|---|---:|---:|
| `createTask(TaskRequest, String)` | raw task fields and optional key | 201 | 400, 422, 500 |
| `getTask(int)` | task id | 200 | 404 |
| `listTasks(TaskQuery)` | optional filters, sort, page, and page size | 200 | 500 |
| `updateTask(int, TaskRequest)` | id and raw task fields | 200 | 400, 404 |
| `deleteTask(int)` | task id | 200 | 404 |
| `createTaskFromText(String, String)` | text and optional key | 201 | 422, 502, 503 |
| `createSubtask(int, TaskRequest)` | parent id and raw task fields | 201 | 400, 404 |
| `listSubtasks(int)` | parent id | 200 | 404 |
| `breakDownTask(int, String)` | task id and optional key | 201 | 400, 404, 422, 502 |
| `listByPriority(String)` | `LOW`, `MEDIUM`, or `HIGH` | 200 | 400 |

`dueDate` must be a real `YYYY-MM-DD` date. `completed` must be exactly
`true` or `false`. Priority defaults to `MEDIUM`.

## Error codes

| Status | Code | Meaning |
|---:|---|---|
| 400 | `VALIDATION_ERROR` | Invalid input or illegal subtask operation |
| 404 | `NOT_FOUND` | Task or parent does not exist |
| 422 | `CONFLICT` | Idempotency key reused with different input |
| 502 | `BAD_GATEWAY` | Model failed or returned invalid output |
| 503 | `SERVICE_UNAVAILABLE` | `LLM_API_KEY` is missing |
| 500 | `INTERNAL_ERROR` | Unexpected server failure |

Idempotency stores only successful responses. A repeated key with identical
input returns the original response and does not create another task or call
the parser again.
