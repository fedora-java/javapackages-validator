package org.fedoraproject.javapackages.validator.util;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import org.fedoraproject.javapackages.validator.ThreadPool;

import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

public abstract class ConcurrentValidator extends ElementwiseValidator {
    protected ConcurrentValidator() {
        this(_ -> true);
    }

    protected ConcurrentValidator(Predicate<RpmInfo> filter) {
        super(filter);
    }

    public final void validate(RpmPackage rpm) throws Exception {
    }

    @Override
    public void validate(Iterable<RpmPackage> rpms) throws Exception {
        var futures = new ArrayList<Future<ElementwiseResultBuilder>>();
        var it = filter(rpms);
        while (it.hasNext()) {
            var rpm = it.next();
            var resultBuilder = spawnValidator();
            futures.add(ThreadPool.submit(() -> {
                try {
                    resultBuilder.validate(rpm);
                } catch (Exception ex) {
                    resultBuilder.error(ex);
                }
                return resultBuilder;
            }));
        }
        for (var future : futures) {
            mergeResult(future.get());
        }
    }

    protected void mergeResult(ElementwiseResultBuilder resultBuilder) {
        mergeResult(resultBuilder.getResult());
        for (var entry : resultBuilder.getLog()) {
            addLog(entry);
        }
    }

    protected abstract ElementwiseResultBuilder spawnValidator();
}
