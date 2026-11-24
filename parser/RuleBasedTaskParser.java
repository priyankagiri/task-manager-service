package parser;

import java.time.LocalDate;

public class RuleBasedTaskParser implements TaskParser {
    @Override
    public ParsedTask parse(String text, String today) {
        if (text == null || text.isBlank())
            return new ParsedTask("", "", null, null);
        String lower = text.toLowerCase();
        String dueDate = null;
        if (lower.contains("tomorrow"))
            dueDate = LocalDate.parse(today).plusDays(1).toString();
        String recurrence = lower.contains("every month") ? "monthly" : null;
        String title = text.trim();
        return new ParsedTask(title, text.trim(), dueDate, recurrence);
    }
}
