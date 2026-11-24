package api;

import exception.ApiException;
import exception.BadGatewayException;
import exception.ConflictException;
import exception.ValidationException;
import model.Priority;
import model.Task;
import model.TaskQuery;
import model.TaskRequest;
import parser.BreakdownStrategy;
import parser.ParsedTask;
import parser.ParserFactory;
import parser.RuleBasedBreakdownStrategy;
import parser.RuleBasedTaskParser;
import parser.TaskParser;
import parser.UnavailableBreakdownStrategy;
import repository.IdempotencyStore;
import service.DateHelper;
import service.TaskService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TaskApi {
    private final TaskService service;
    private final TaskParser parser;
    private final BreakdownStrategy breakdownStrategy;
    private final IdempotencyStore idempotency = IdempotencyStore.getInstance();

    public TaskApi(TaskService service) {
        this(service, ParserFactory.create(), new UnavailableBreakdownStrategy());
    }
    public TaskApi(TaskService service, TaskParser parser, BreakdownStrategy breakdownStrategy) {
        this.service = service; this.parser = parser; this.breakdownStrategy = breakdownStrategy;
    }
    public ApiResponse createTask(TaskRequest request, String key) {
        return execute(() -> idempotent("create:" + fingerprint(request), key, () -> taskResponse(201, service.create(request))));
    }
    public ApiResponse getTask(int id) { return execute(() -> taskResponse(200, service.get(id))); }
    public ApiResponse updateTask(int id, TaskRequest request) { return execute(() -> taskResponse(200, service.update(id, request))); }
    public ApiResponse deleteTask(int id) {
        return execute(() -> { service.delete(id); Map<String, Object> body = new LinkedHashMap<>(); body.put("deleted", true); body.put("id", id); return new ApiResponse(200, body); });
    }
    public ApiResponse listTasks(TaskQuery query) {
        return execute(() -> {
            List<Map<String, Object>> body = new ArrayList<>();
            for (Task task : service.list(query == null ? TaskQuery.builder().build() : query)) body.add(task.toMap());
            return new ApiResponse(200, body);
        });
    }
    public ApiResponse listByPriority(String level) {
        return execute(() -> {
            Priority priority;
            try { priority = Priority.valueOf(level); }
            catch (Exception e) { throw new ValidationException("priority", "must be LOW, MEDIUM or HIGH"); }
            return listTasks(TaskQuery.builder().priority(priority).build());
        });
    }
    public ApiResponse createSubtask(int parentId, TaskRequest request) {
        return execute(() -> taskResponse(201, service.createSubtask(parentId, request)));
    }
    public ApiResponse listSubtasks(int parentId) {
        return execute(() -> {
            List<Map<String, Object>> body = new ArrayList<>();
            for (Task task : service.subtasks(parentId)) body.add(task.toMap());
            return new ApiResponse(200, body);
        });
    }
    public ApiResponse createTaskFromText(String text, String key) {
        return execute(() -> idempotent("text:" + fingerprint(text), key, () -> {
            ParsedTask parsed = parser.parse(text, DateHelper.today());
            if (parsed == null) throw new BadGatewayException("The language model returned no task");
            TaskRequest request = new TaskRequest(parsed.getTitle(), parsed.getDescription(), "false", parsed.getDueDate(), "MEDIUM");
            try {
                service.validate(request);
                return taskResponse(201, service.create(request));
            } catch (ValidationException e) {
                throw new BadGatewayException("The language model returned an invalid task", e.getIssues());
            }
        }));
    }
    public ApiResponse breakDownTask(int id, String key) {
        return execute(() -> idempotent("breakdown:" + id, key, () -> {
            Task parent = service.get(id);
            if (parent.getParentId() != null) throw new ValidationException("parentId", "a subtask cannot be broken down");
            List<TaskRequest> requests = breakdownStrategy.breakDown(parent.getTitle(), parent.getDescription());
            if (requests == null || requests.isEmpty()) throw new BadGatewayException("The breakdown returned no subtasks");
            for (TaskRequest request : requests) {
                try { service.validate(request); }
                catch (ValidationException e) { throw new BadGatewayException("The language model returned an invalid subtask", e.getIssues()); }
            }
            List<Task> created = new ArrayList<>();
            for (TaskRequest request : requests) created.add(service.createSubtask(id, request));
            List<Map<String, Object>> body = new ArrayList<>();
            for (Task task : created) body.add(task.toMap());
            return new ApiResponse(201, body);
        }));
    }

    private ApiResponse taskResponse(int status, Task task) { return new ApiResponse(status, task.toMap()); }
    private ApiResponse execute(Operation operation) {
        try { return operation.run(); }
        catch (ApiException e) { return ErrorResponseFactory.from(e); }
        catch (Exception e) { return ErrorResponseFactory.fromUnexpected(e); }
    }
    private ApiResponse idempotent(String fingerprint, String key, Operation operation) {
        if (key == null || key.isBlank()) return operation.run();
        IdempotencyStore.Record record = idempotency.find(key);
        if (record != null) {
            if (!record.getFingerprint().equals(fingerprint)) throw new ConflictException("Idempotency key reused with a different request");
            return record.getResponse();
        }
        ApiResponse response = operation.run();
        if (response.getStatus() >= 200 && response.getStatus() < 300) idempotency.save(key, fingerprint, response);
        return response;
    }
    private static String fingerprint(TaskRequest request) {
        if (request == null) return "null";
        return fingerprint(request.getTitle(), request.getDescription(), request.getCompleted(), request.getDueDate(), request.getPriority());
    }
    private static String fingerprint(String... values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) { String v = String.valueOf(value); result.append(v.length()).append(':').append(v).append(';'); }
        return result.toString();
    }
    @FunctionalInterface private interface Operation { ApiResponse run(); }
}
