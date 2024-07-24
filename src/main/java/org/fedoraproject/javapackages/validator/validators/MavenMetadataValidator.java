package org.fedoraproject.javapackages.validator.validators;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.util.Common;
import org.fedoraproject.javapackages.validator.util.ElementwiseValidator;
import org.fedoraproject.xmvn.metadata.PackageMetadata;
import org.fedoraproject.xmvn.metadata.io.stax.MetadataStaxReader;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.kojan.javadeptools.rpm.RpmArchiveInputStream;
import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;

/**
 * Validator which checks that maven metadata XML file references files
 * present on the file system relative to the environment root.
 *
 * Ignores source rpms.
 */
@SuppressFBWarnings({"DMI_HARDCODED_ABSOLUTE_FILENAME"})
public class MavenMetadataValidator extends ElementwiseValidator {
    @Override
    public String getTestName() {
        return "/java/maven-metadata";
    }

    public MavenMetadataValidator() {
        super(Predicate.not(RpmInfo::isSourcePackage));
    }

    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "Incorrect claim that Exception is never thrown")
    @Override
    public void validate(RpmPackage rpm) throws Exception {
        var metadataXmls = new ArrayList<Map.Entry<CpioArchiveEntry, byte[]>>();
        var foundFiles = new TreeSet<String>();
        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            for (CpioArchiveEntry rpmEntry; (rpmEntry = is.getNextEntry()) != null;) {
                foundFiles.add(Common.getEntryPath(rpmEntry).toString());
                if (rpmEntry.getName().startsWith("./usr/share/maven-metadata/") && rpmEntry.getName().endsWith(".xml")) {
                    byte[] content = new byte[(int) rpmEntry.getSize()];
                    if (is.read(content) != content.length) {
                        throw Common.INCOMPLETE_READ;
                    }
                    metadataXmls.add(Map.entry(rpmEntry, content));
                }
            }
        }

        if (metadataXmls.isEmpty()) {
            skip("{0}: maven metadata XML file not found", Decorated.rpm(rpm));
        }

        var jarsWithoutMd = foundFiles.stream()
                .filter(f -> f.startsWith("/usr/share/java/"))
                .filter(f -> f.endsWith(".jar"))
                .collect(Collectors.toSet());

        for (var entry : metadataXmls) {
            PackageMetadata packageMetadata = null;

            try (var is = new ByteArrayInputStream(entry.getValue())) {
                packageMetadata = new MetadataStaxReader().read(is, true);
            } catch (XMLStreamException ex) {
                fail("{0}: metadata validation failed: {1}", Decorated.rpm(rpm), Decorated.plain(ex.getMessage()));
                continue;
            }

            for (var artifact : packageMetadata.getArtifacts()) {
                var artifactPath = Paths.get(artifact.getPath());
                var metadataXml = Common.getEntryPath(entry.getKey());
                jarsWithoutMd.remove(artifactPath.toString());
                if (foundFiles.contains(artifactPath.toString())) {
                    pass("{0}: {1}: artifact {2} is present in the RPM",
                            Decorated.rpm(rpm),
                            Decorated.outer(metadataXml),
                            Decorated.actual(artifactPath));
                } else {
                    fail("{0}: {1}: artifact {2} is not present in the RPM",
                            Decorated.rpm(rpm),
                            Decorated.outer(metadataXml),
                            Decorated.expected(artifactPath));
                }
            }
        }

        for (var jar : jarsWithoutMd) {
            info("{0}: JAR file without corresponding Maven metadata: {1}",
                    Decorated.rpm(rpm),
                    Decorated.actual(jar));
        }
    }
}
