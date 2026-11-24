package app;

import api.ApiResponse;
import api.TaskApi;
import model.TaskRequest;
import repository.InMemoryTaskRepository;
import parser.RuleBasedBreakdownStrategy;
import parser.RuleBasedTaskParser;
import service.TaskService;

public class Main {
    public static void main(String[] args) {
        TaskApi api = new TaskApi(new TaskService(InMemoryTaskRepository.getInstance()),
                new RuleBasedTaskParser(), new RuleBasedBreakdownStrategy());
        ApiResponse response = api.createTask(
                new TaskRequest("Pay rent", "Pay monthly rent", "false", "2026-12-01", "HIGH"), "main-1");
        System.out.println(response);
    }
}
