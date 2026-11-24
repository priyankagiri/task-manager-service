package model;

public final class TaskQuery {
    private final Boolean completed;
    private final Priority priority;
    private final String search;
    private final String sortBy;
    private final String order;
    private final int page;
    private final int pageSize;

    private TaskQuery(Builder b) {
        completed = b.completed;
        priority = b.priority;
        search = b.search;
        sortBy = b.sortBy;
        order = b.order;
        page = b.page;
        pageSize = b.pageSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Boolean getCompleted() {
        return completed;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getSearch() {
        return search;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getOrder() {
        return order;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public static final class Builder {
        private Boolean completed;
        private Priority priority;
        private String search;
        private String sortBy;
        private String order = "asc";
        private int page = 1;
        private int pageSize = 10;

        public Builder completed(Boolean v) {
            completed = v;
            return this;
        }

        public Builder priority(Priority v) {
            priority = v;
            return this;
        }

        public Builder search(String v) {
            search = v;
            return this;
        }

        public Builder sortBy(String v) {
            sortBy = v;
            return this;
        }

        public Builder order(String v) {
            order = v;
            return this;
        }

        public Builder page(int v) {
            page = v;
            return this;
        }

        public Builder pageSize(int v) {
            pageSize = v;
            return this;
        }

        public TaskQuery build() {
            return new TaskQuery(this);
        }
    }
}
