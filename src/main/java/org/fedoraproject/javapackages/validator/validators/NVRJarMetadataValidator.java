package org.fedoraproject.javapackages.validator.validators;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarInputStream;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javapackages.validator.DefaultValidator;
import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.util.Common;
import org.fedoraproject.javapackages.validator.util.JarValidator;
import org.fedoraproject.javapackages.validator.util.RpmJarConsumer;

import io.kojan.javadeptools.rpm.RpmPackage;

public class NVRJarMetadataValidator extends DefaultValidator {
    @Override
    public String getTestName() {
        return "/java/nvr-jar-metadata";
    }

    private static interface Entry {
        String name();
        String valueOf(RpmPackage rpm);
    }

    private static class RpmName implements Entry {
        @Override
        public String name() {
            return "Rpm-Name";
        }
        @Override
        public String valueOf(RpmPackage rpm) {
            return rpm.getInfo().getName();
        }
    }

    private static class RpmEpoch implements Entry {
        @Override
        public String name() {
            return "Rpm-Epoch";
        }
        @Override
        public String valueOf(RpmPackage rpm) {
            return rpm.getInfo().getEpoch().map(String::valueOf).orElse("");
        }
    }

    private static class RpmVersion implements Entry {
        @Override
        public String name() {
            return "Rpm-Version";
        }
        @Override
        public String valueOf(RpmPackage rpm) {
            return rpm.getInfo().getVersion();
        }
    }

    private static class RpmRelease implements Entry {
        @Override
        public String name() {
            return "Rpm-Release";
        }
        @Override
        public String valueOf(RpmPackage rpm) {
            return rpm.getInfo().getRelease();
        }
    }

    private static List<Entry> ENTRIES = List.of(new RpmName(), new RpmEpoch(), new RpmVersion(), new RpmRelease());

    private class RpmEntry implements RpmJarConsumer {
        RpmPackage sourceRpm = null;
        List<RpmPackage> binaryRpms = new ArrayList<>();

        @Override
        public void acceptJarEntry(RpmPackage rpm, CpioArchiveEntry rpmEntry, byte[] content) throws Exception {
            var jarPath = Path.of(rpmEntry.getName());
            var standardLocations = List.of("/usr/share/java", "/usr/lib/java");
            if (standardLocations.stream().anyMatch(jarPath::startsWith)) {
                try (var is = new JarInputStream(new ByteArrayInputStream(content))) {
                    var mf = is.getManifest();
                    var attrs = mf.getMainAttributes();

                    for (var entry : ENTRIES) {
                        var srpmValue = entry.valueOf(sourceRpm);
                        var attrValue = attrs.getValue(entry.name());

                        if (attrValue == null) {
                            fail("{0}: {1}: Jar manifest attribute {2} is not present",
                                    Decorated.rpm(rpm),
                                    Decorated.custom(Common.getEntryPath(rpmEntry), JarValidator.DECORATION_JAR),
                                    Decorated.struct(entry.name()));
                        } else if (srpmValue.equals(attrValue)) {
                            pass("{0}: {1}: Jar manifest attribute {2} with value \"{3}\" matches the RPM attribute",
                                    Decorated.rpm(rpm),
                                    Decorated.custom(Common.getEntryPath(rpmEntry), JarValidator.DECORATION_JAR),
                                    Decorated.struct(entry.name()),
                                    Decorated.actual(attrValue));
                        } else {
                            fail("{0}: {1}: Jar manifest attribute {2} with value \"{3}\" does not match the RPM attribute value \"{4}\"",
                                    Decorated.rpm(rpm),
                                    Decorated.custom(Common.getEntryPath(rpmEntry), JarValidator.DECORATION_JAR),
                                    Decorated.struct(entry.name()),
                                    Decorated.actual(attrValue),
                                    Decorated.expected(srpmValue));
                        }
                    }
                }
            } else {
                skip("{0}: Ignoring Jar file {1} installed in a non-standard location: none of {2}",
                        Decorated.rpm(rpm),
                        Decorated.custom(Common.getEntryPath(rpmEntry), JarValidator.DECORATION_JAR),
                        Decorated.outer(standardLocations));
            }
        }
    }

    private Map<String, RpmEntry> rpms;

    public NVRJarMetadataValidator() {
        this.rpms = new TreeMap<>();
    }

    @Override
    public void validate(Iterable<RpmPackage> rpms) throws Exception {
        for (var rpm : rpms) {
            if (rpm.getInfo().isSourcePackage()) {
                var filename = rpm.getPath().getFileName();
                if (filename == null) {
                    error("{0}: Could not obtain the path from URL: {1}",
                            Decorated.rpm(rpm), Decorated.actual(rpm.getPath()));
                    return;
                }
                this.rpms.computeIfAbsent(filename.toString(), _ -> new RpmEntry()).sourceRpm = rpm;
            } else {
                this.rpms.computeIfAbsent(rpm.getInfo().getSourceRPM(), _ -> new RpmEntry()).binaryRpms.add(rpm);
            }
        }

        for (var entry : this.rpms.values()) {
            for (var binary : entry.binaryRpms) {
                entry.accept(binary);
            }
        }
    }
}
