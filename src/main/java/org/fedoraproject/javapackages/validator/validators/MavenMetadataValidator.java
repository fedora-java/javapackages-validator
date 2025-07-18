package org.fedoraproject.javapackages.validator.validators;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.util.Common;
import org.fedoraproject.javapackages.validator.util.ElementwiseValidator;
import org.fedoraproject.xmvn.metadata.PackageMetadata;

import io.kojan.javadeptools.rpm.RpmArchiveInputStream;
import io.kojan.javadeptools.rpm.RpmInfo;
import io.kojan.javadeptools.rpm.RpmPackage;
import io.kojan.xml.XMLException;

/// Validator which checks that Maven metadata XML stored in
/// `/usr/share/maven-metadata/`.
///
/// It checks whether:
///
/// - the metadata files can be parsed with XMvn,
/// - all artifacts mentioned in the metadata are actually included as files in
///   the package,
/// - there are no JARs and POMs without Maven metadata.
///
/// Ignores source RPMs.
public class MavenMetadataValidator extends ElementwiseValidator {
    @Override
    public String getTestName() {
        return "/java/maven-metadata";
    }

    public MavenMetadataValidator() {
        super(Predicate.not(RpmInfo::isSourcePackage));
    }

    @Override
    public void validate(RpmPackage rpm) throws Exception {
        var metadataXmls = new ArrayList<Map.Entry<CpioArchiveEntry, byte[]>>();
        var foundFiles = new TreeSet<String>();
        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            for (CpioArchiveEntry rpmEntry; (rpmEntry = is.getNextEntry()) != null;) {
                if (!rpmEntry.isRegularFile()) {
                    continue;
                }
                foundFiles.add(Common.getEntryPath(rpmEntry).toString());
                if (rpmEntry.getName().startsWith("/usr/share/maven-metadata/") && rpmEntry.getName().endsWith(".xml")) {
                    metadataXmls.add(Map.entry(rpmEntry, IOUtils.toByteArray(is)));
                }
            }
        }

        if (metadataXmls.isEmpty()) {
            skip("{0}: maven metadata XML file not found", Decorated.rpm(rpm));
        }

        var jarsWithoutMd = foundFiles.stream()
                .filter(f -> f.startsWith("/usr/share/java/") || f.startsWith("/usr/lib/java/"))
                .filter(f -> f.endsWith(".jar"))
                .collect(Collectors.toSet());
        var pomsWithoutMd = foundFiles.stream()
                .filter(f -> f.startsWith("/usr/share/maven-poms/"))
                .filter(f -> f.endsWith(".pom"))
                .collect(Collectors.toSet());

        for (var entry : metadataXmls) {
            PackageMetadata packageMetadata = null;

            try {
                String xml = new String(entry.getValue(), StandardCharsets.UTF_8);
                packageMetadata = PackageMetadata.fromXML(xml);
            } catch (XMLException ex) {
                fail("{0}: metadata validation failed: {1}", Decorated.rpm(rpm), Decorated.plain(ex.getMessage()));
                continue;
            }

            for (var artifact : packageMetadata.getArtifacts()) {
                var artifactPath = Path.of(artifact.getPath());
                var metadataXml = Common.getEntryPath(entry.getKey());
                jarsWithoutMd.remove(artifactPath.toString());
                pomsWithoutMd.remove(artifactPath.toString());
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
        for (var pom : pomsWithoutMd) {
            info("{0}: POM file without corresponding Maven metadata: {1}",
                    Decorated.rpm(rpm),
                    Decorated.actual(pom));
        }
    }
}
