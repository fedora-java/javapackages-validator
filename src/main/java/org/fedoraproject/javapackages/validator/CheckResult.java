package org.fedoraproject.javapackages.validator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class CheckResult {
    private final List<Pair<String, Decorated[]>> messages = new ArrayList<>();

    public void add(String pattern, Decorated... arguments) {
        messages.add(Pair.of(pattern, arguments));
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

    public void printMessages(Logger logger) {
        for (var message : messages) {
            logger.fail(message.getKey(), message.getValue());
        }
    }
}
