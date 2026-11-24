package event;

import model.Task;

public class ConsoleLogListener implements TaskEventListener {
    @Override
    public void onEvent(String eventType, Task task) {
        System.out.println("EVENT " + eventType + " task=" + task.getId());
    }
}
