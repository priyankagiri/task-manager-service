package model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Task {
    private final int id;
    private final String title;
    private final String description;
    private final boolean completed;
    private final String dueDate;
    private final Priority priority;
    private final Integer parentId;
    private final long createdAt;

    private Task(Builder b) {
        this.id = b.id;
        this.title = b.title;
        this.description = b.description;
        this.completed = b.completed;
        this.dueDate = b.dueDate;
        this.priority = b.priority == null ? Priority.MEDIUM : b.priority;
        this.parentId = b.parentId;
        this.createdAt = b.createdAt == 0 ? System.currentTimeMillis() : b.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getDueDate() {
        return dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public Integer getParentId() {
        return parentId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("description", description);
        map.put("completed", completed);
        map.put("dueDate", dueDate);
        map.put("priority", priority.name());
        map.put("parentId", parentId);
        map.put("createdAt", createdAt);
        return map;
    }

    public static final class Builder {
        private int id;
        private String title;
        private String description;
        private boolean completed;
        private String dueDate;
        private Priority priority = Priority.MEDIUM;
        private Integer parentId;
        private long createdAt;

        public Builder id(int value) {
            id = value;
            return this;
        }

        public Builder title(String value) {
            title = value;
            return this;
        }

        public Builder description(String value) {
            description = value;
            return this;
        }

        public Builder completed(boolean value) {
            completed = value;
            return this;
        }

        public Builder dueDate(String value) {
            dueDate = value;
            return this;
        }

        public Builder priority(Priority value) {
            priority = value;
            return this;
        }

        public Builder parentId(Integer value) {
            parentId = value;
            return this;
        }

        public Builder createdAt(long value) {
            createdAt = value;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }
}
