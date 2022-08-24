package org.fedoraproject.javapackages.validator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckResult {
    private final List<String> messages = new ArrayList<>();

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void add(String pattern, Decorated... arguments) {
        messages.add(MessageFormat.format(pattern, arguments));
    }

    public void combineWith(CheckResult other) {
        messages.addAll(other.messages);
    }

    public boolean isPass() {
        return messages.isEmpty();
    }

    public int getFailureCount() {
        return messages.size();
    }
}
