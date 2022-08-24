/*-
 * Copyright (c) 2022 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.javapackages.validator.checks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.fedoraproject.javadeptools.rpm.RpmArchiveInputStream;
import org.fedoraproject.javapackages.validator.Common;
import org.fedoraproject.javapackages.validator.Decorated;
import org.fedoraproject.javapackages.validator.ElementwiseCheck;
import org.fedoraproject.javapackages.validator.RpmPathInfo;
import org.fedoraproject.javapackages.validator.TextDecorator.Decoration;
import org.fedoraproject.javapackages.validator.config.BytecodeVersionConfig;

public class BytecodeVersionCheck extends ElementwiseCheck<BytecodeVersionConfig> {
    public BytecodeVersionCheck() {
        super(BytecodeVersionConfig.class);
    }

    @Override
    public Collection<String> check(BytecodeVersionConfig config, RpmPathInfo rpm) throws IOException {
        var result = new ArrayList<String>(0);

        try (var is = new RpmArchiveInputStream(rpm.getPath())) {
            for (CpioArchiveEntry rpmEntry; ((rpmEntry = is.getNextEntry()) != null);) {
                var content = new byte[(int) rpmEntry.getSize()];

                if (is.read(content) != content.length) {
                    throw Common.INCOMPLETE_READ;
                }

                if (!rpmEntry.isSymbolicLink() && rpmEntry.getName().endsWith(".jar")) {
                    String jarName = rpmEntry.getName().substring(1);

                    try (var jarStream = new JarArchiveInputStream(new ByteArrayInputStream(content))) {
                        var foundVersions = new TreeSet<Short>();
                        for (JarArchiveEntry jarEntry; ((jarEntry = jarStream.getNextJarEntry()) != null);) {
                            String className = jarEntry.getName();

                            if (className.endsWith(".class")) {
                                // Read 6-th and 7-th bytes which indicate the .class bytecode version
                                if (jarStream.skip(6) != 6) {
                                    throw Common.INCOMPLETE_READ;
                                }

                                // ByteBuffer's initial byte order is big-endian
                                // which is the same as is used in java .class files
                                var versionBuffer = ByteBuffer.allocate(2);

                                if (jarStream.read(versionBuffer.array()) != versionBuffer.capacity()) {
                                    throw Common.INCOMPLETE_READ;
                                }

                                var version = versionBuffer.getShort();

                                if (!config.allowedVersion(rpm, jarName, className, version)) {
                                    result.add(failMessage("{0}: {1}: {2}: illegal class bytecode version {3}",
                                            Decorated.rpm(rpm.getPath()),
                                            Decorated.outer(jarName),
                                            Decorated.custom(className, Decoration.bright_yellow),
                                            Decorated.actual(version)));
                                    foundVersions = null;
                                } else if (foundVersions != null) {
                                    foundVersions.add(version);
                                }
                            }
                        }

                        if (foundVersions != null) {
                            getLogger().pass("{0}: {1}: found bytecode versions: {2}",
                                    Decorated.rpm(rpm.getPath()),
                                    Decorated.outer(jarName),
                                    Decorated.actual(foundVersions));
                        }
                    }
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.exit(new BytecodeVersionCheck().executeCheck(args));
    }
}
