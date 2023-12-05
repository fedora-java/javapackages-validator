package org.fedoraproject.javapackages.validator.validators;

import java.io.ByteArrayInputStream;
import java.lang.module.ModuleDescriptor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.fedoraproject.javapackages.validator.spi.Decorated;
import org.fedoraproject.javapackages.validator.util.Common;
import org.fedoraproject.javapackages.validator.util.JarValidator;

import io.kojan.javadeptools.rpm.RpmPackage;

public class JpmsProvidesValidator extends JarValidator {
    @Override
    public String getTestName() {
        return "/java/jpms-provides";
    }

    private Map<String, String> jarModuleNames = new TreeMap<>();

    private static Pattern VERSIONS_PATTERN = Pattern.compile("META-INF/versions/\\d+/module-info\\.class");

    @Override
    public void acceptJarEntry(RpmPackage rpm, CpioArchiveEntry rpmEntry, byte[] content) throws Exception {
        var moduleNames = new ArrayList<Map.Entry<String, String>>();
        var rpmEntryString = Common.getEntryPath(rpmEntry).toString();

        try (var is = new JarInputStream(new ByteArrayInputStream(content))) {
            for (JarEntry entry; (entry = is.getNextJarEntry()) != null;) {
                if (entry.getName().equals("module-info.class")
                        || (entry.getName().startsWith("META-INF/versions/")
                            && VERSIONS_PATTERN.matcher(entry.getName()).matches())) {
                    var md = ModuleDescriptor.read(ByteBuffer.wrap(is.readNBytes((int) entry.getSize())));
                    moduleNames.add(Map.entry(entry.getName(), md.name()));
                }
            }

            if (moduleNames.isEmpty()) {
                var mf = is.getManifest();
                var moduleName = mf.getMainAttributes().getValue("Automatic-Module-Name");
                if (moduleName != null) {
                    moduleNames.add(Map.entry("META-INF/MANIFEST.MF:Automatic-Module-Name", moduleName));
                }
            }
        }

        for (var entry : moduleNames) {
            debug("{0}: {1}: {2}: Found module name: {3}",
                    Decorated.rpm(rpm),
                    Decorated.outer(rpmEntryString),
                    Decorated.struct(entry.getKey()),
                    Decorated.actual(entry.getValue()));
        }

        String moduleName = null;
        for (var entry : moduleNames) {
            if (moduleName == null) {
                moduleName = entry.getValue();
                jarModuleNames.put(rpmEntryString, moduleName);
            } else if (! moduleName.equals(entry.getValue())) {
                fail("{0}: {1}: Differing module names: {2} and {3}",
                        Decorated.rpm(rpm),
                        Decorated.outer(rpmEntryString),
                        Decorated.struct(moduleName),
                        Decorated.actual(entry.getValue()));
            }
        }
    }

    @Override
    public void validate(RpmPackage rpm) throws Exception {
        var providedModuleNames = new TreeSet<String>();

        for (var reldep : rpm.getInfo().getProvides()) {
            var name = reldep.getName();
            if (name.startsWith("jpms(") && name.endsWith(")")) {
                providedModuleNames.add(name);
                debug("{0}: Provides JPMS name: {1}", Decorated.rpm(rpm), Decorated.actual(name));
            }
        }

        super.validate(rpm);
        boolean ok = true;

        for (var providedModuleName : providedModuleNames) {
            if (! jarModuleNames.values().contains(providedModuleName)) {
                ok = false;
                fail("{0}: Module name {1} provided by this RPM was not found in any JAR file",
                        Decorated.rpm(rpm),
                        Decorated.actual(providedModuleName));
            }
        }

        for (var jarModuleNameEntry : jarModuleNames.entrySet()) {
            if (! providedModuleNames.contains(jarModuleNameEntry.getValue())) {
                ok = false;
                fail("{0}: {1}: Module name {2} is not provided by this RPM",
                        Decorated.rpm(rpm),
                        Decorated.outer(jarModuleNameEntry.getKey()),
                        Decorated.actual(jarModuleNameEntry.getValue()));
            }
        }

        if (ok) {
            pass("{0}: found module names exactly match provided JPMS fields, {1}",
                    Decorated.rpm(rpm),
                    Decorated.actual(providedModuleNames.stream().toList()));
        }
    }
}
