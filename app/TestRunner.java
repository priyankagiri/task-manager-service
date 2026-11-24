package app;

import api.ApiResponse;
import api.TaskApi;
import model.TaskQuery;
import model.TaskRequest;
import parser.BreakdownStrategy;
import parser.LlmTaskParser;
import parser.LlmClient;
import parser.RuleBasedBreakdownStrategy;
import parser.RuleBasedTaskParser;
import repository.IdempotencyStore;
import repository.InMemoryTaskRepository;
import service.TaskService;
import service.DateHelper;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        check("2026-02-30 is rejected as a date", false, DateHelper.isValidDate("2026-02-30"));
        InMemoryTaskRepository repository = InMemoryTaskRepository.getInstance();
        repository.clear();
        IdempotencyStore.getInstance().clear();
        TaskService service = new TaskService(repository);
        TaskApi api = new TaskApi(service, new RuleBasedTaskParser(), new RuleBasedBreakdownStrategy());

        TaskRequest rent = new TaskRequest("Pay rent", "Monthly rent", "false", "2026-12-01", "HIGH");
        ApiResponse created = api.createTask(rent, "rent-1");
        check("create returns 201", 201, created.getStatus());
        check("same idempotency request replays", created.toJson(), api.createTask(rent, "rent-1").toJson());
        check("reused key with different request returns 422", 422,
                api.createTask(new TaskRequest("Other", "Other", "false", null, "LOW"), "rent-1").getStatus());
        check("invalid input returns 400", 400,
                api.createTask(new TaskRequest("", "", "maybe", "2026-02-30", "URGENT"), null).getStatus());
        check("unknown task returns 404", 404, api.getTask(999).getStatus());
        check("filter and pagination work", 1,
                ((List<?>) api.listTasks(TaskQuery.builder().priority(
                        model.Priority.HIGH).pageSize(1).build()).getBody()).size());

        ApiResponse parent = api.createTask(new TaskRequest("Prepare trip", "Book flight and pack bags",
                "false", null, "MEDIUM"), null);
        int parentId = (Integer) ((java.util.Map<?, ?>) parent.getBody()).get("id");
        check("breakdown creates subtasks", 201, api.breakDownTask(parentId, null).getStatus());
        check("delete parent succeeds", 200, api.deleteTask(parentId).getStatus());
        check("delete parent removes subtasks", 404, api.listSubtasks(parentId).getStatus());

        LlmClient fake = new LlmClient() {
            @Override
            public String send(String prompt) throws IOException {
                return "{\"title\":\"Call doctor\",\"description\":\"Make appointment\",\"dueDate\":null,\"recurrence\":null}";
            }
        };
        check("valid fake model output parses", "Call doctor",
                new LlmTaskParser(fake).parse("call doctor", "2026-01-01").getTitle());
        LlmClient faulty = prompt -> "{\"title\":\"\",\"description\":\"x\",\"dueDate\":\"2026-02-30\"}";
        TaskApi faultyApi = new TaskApi(new TaskService(repository), new LlmTaskParser(faulty),
                new BreakdownStrategy() {
                    public List<TaskRequest> breakDown(String t, String d) {
                        return List.of();
                    }
                });
        check("bad model output returns 502", 502, faultyApi.createTaskFromText("bad", null).getStatus());
        check("missing model key returns 503", 503,
                new TaskApi(new TaskService(repository)).createTaskFromText("call doctor", null).getStatus());

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /**
     * call -> compare -> print. 'expected' and 'actual' can be any type; equals()
     * decides.
     */
    static void check(String name, Object expected, Object actual) {
        if (Objects.equals(expected, actual)) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name + "  (expected: " + expected + ", actual: " + actual + ")");
        }
    }
}
