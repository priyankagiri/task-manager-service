package event;

import model.Task;
import java.util.HashMap;
import java.util.Map;

public class AuditTrailListener implements TaskEventListener {
    private final Map<String, Integer> counts = new HashMap<>();

    @Override
    public void onEvent(String eventType, Task task) {
        counts.put(eventType, counts.getOrDefault(eventType, 0) + 1);
    }

    public int count(String eventType) {
        return counts.getOrDefault(eventType, 0);
    }
}
