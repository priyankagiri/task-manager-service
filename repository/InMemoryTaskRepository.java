package repository;

import model.Task;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class InMemoryTaskRepository implements TaskRepository {
    private static final InMemoryTaskRepository INSTANCE = new InMemoryTaskRepository();
    private final Map<Integer, Task> tasks = new LinkedHashMap<>();
    private int nextId = 1;

    private InMemoryTaskRepository() {
    }

    public static InMemoryTaskRepository getInstance() {
        return INSTANCE;
    }

    @Override
    public synchronized Task save(Task task) {
        Task stored = task.getId() == 0 ? Task.builder().id(nextId++).title(task.getTitle())
                .description(task.getDescription()).completed(task.isCompleted()).dueDate(task.getDueDate())
                .priority(task.getPriority()).parentId(task.getParentId()).createdAt(task.getCreatedAt()).build()
                : task;
        tasks.put(stored.getId(), stored);
        return stored;
    }

    @Override
    public synchronized Optional<Task> findById(int id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public synchronized List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public synchronized boolean delete(int id) {
        return tasks.remove(id) != null;
    }

    @Override
    public synchronized void clear() {
        tasks.clear();
        nextId = 1;
    }
}
