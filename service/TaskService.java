package service;

import event.TaskEventListener;
import exception.FieldIssue;
import exception.NotFoundException;
import exception.ValidationException;
import model.Priority;
import model.Task;
import model.TaskQuery;
import model.TaskRequest;
import repository.TaskRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TaskService {
    private final TaskRepository repository;
    private final List<TaskEventListener> listeners = new ArrayList<>();

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public void addListener(TaskEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TaskEventListener listener) {
        listeners.remove(listener);
    }

    public Task create(TaskRequest request) {
        return save(request, null);
    }

    public void validate(TaskRequest request) {
        build(request, 0, 0, null);
    }

    public Task createSubtask(int parentId, TaskRequest request) {
        Task parent = get(parentId);
        if (parent.getParentId() != null)
            throw new ValidationException("parentId", "a subtask cannot have subtasks");
        return save(request, parentId);
    }

    public Task update(int id, TaskRequest request) {
        Task old = get(id);
        Task updated = build(request, id, old.getCreatedAt(), old.getParentId());
        Task saved = repository.save(updated);
        notifyListeners("updated", saved);
        return saved;
    }

    public Task get(int id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Task " + id + " not found"));
    }

    public List<Task> subtasks(int parentId) {
        get(parentId);
        List<Task> result = new ArrayList<>();
        for (Task task : repository.findAll())
            if (Integer.valueOf(parentId).equals(task.getParentId()))
                result.add(task);
        return result;
    }

    public void delete(int id) {
        Task task = get(id);
        for (Task child : new ArrayList<>(subtasks(id)))
            delete(child.getId());
        repository.delete(id);
        notifyListeners("deleted", task);
    }

    public List<Task> list(TaskQuery query) {
        List<Task> result = new ArrayList<>();
        for (Task task : repository.findAll()) {
            if (query.getCompleted() != null && task.isCompleted() != query.getCompleted())
                continue;
            if (query.getPriority() != null && task.getPriority() != query.getPriority())
                continue;
            if (query.getSearch() != null && !query.getSearch().isBlank()) {
                String search = query.getSearch().toLowerCase(Locale.ROOT);
                if (!task.getTitle().toLowerCase(Locale.ROOT).contains(search)
                        && !task.getDescription().toLowerCase(Locale.ROOT).contains(search))
                    continue;
            }
            result.add(task);
        }
        Comparator<Task> comparator = "dueDate".equalsIgnoreCase(query.getSortBy())
                ? Comparator.comparing(t -> t.getDueDate() == null ? "" : t.getDueDate())
                : Comparator.comparingLong(Task::getCreatedAt);
        if ("desc".equalsIgnoreCase(query.getOrder()))
            comparator = comparator.reversed();
        result.sort(comparator);
        int page = Math.max(1, query.getPage()), size = Math.max(1, query.getPageSize());
        int from = Math.min((page - 1) * size, result.size());
        return new ArrayList<>(result.subList(from, Math.min(from + size, result.size())));
    }

    private Task save(TaskRequest request, Integer parentId) {
        Task task = repository.save(build(request, 0, 0, parentId));
        notifyListeners("created", task);
        return task;
    }

    private Task build(TaskRequest request, int id, long createdAt, Integer parentId) {
        List<FieldIssue> issues = new ArrayList<>();
        if (request == null) {
            throw new ValidationException("request", "must not be null");
        }
        if (request.getTitle() == null || request.getTitle().isBlank())
            issues.add(new FieldIssue("title", "must not be empty"));
        if (request.getDescription() == null || request.getDescription().isBlank())
            issues.add(new FieldIssue("description", "must not be empty"));
        if (!"true".equals(request.getCompleted()) && !"false".equals(request.getCompleted()))
            issues.add(new FieldIssue("completed", "must be exactly true or false"));
        if (request.getDueDate() != null && !request.getDueDate().isBlank()
                && !DateHelper.isValidDate(request.getDueDate()))
            issues.add(new FieldIssue("dueDate", "must be a real YYYY-MM-DD date"));
        Priority priority = Priority.MEDIUM;
        if (request.getPriority() != null && !request.getPriority().isBlank()) {
            try {
                priority = Priority.valueOf(request.getPriority());
            } catch (IllegalArgumentException e) {
                issues.add(new FieldIssue("priority", "must be LOW, MEDIUM or HIGH"));
            }
        }
        if (!issues.isEmpty())
            throw new ValidationException(issues);
        return Task.builder().id(id).title(request.getTitle().trim()).description(request.getDescription().trim())
                .completed(Boolean.parseBoolean(request.getCompleted())).dueDate(blankToNull(request.getDueDate()))
                .priority(priority).parentId(parentId).createdAt(createdAt).build();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void notifyListeners(String type, Task task) {
        for (TaskEventListener listener : new ArrayList<>(listeners))
            listener.onEvent(type, task);
    }
}
