package org.fedoraproject.javapackages.validator.config;

public interface BytecodeVersionConfig {
    public static final class VersionRange {
        public final int min;
        public final int max;

        public VersionRange(int min, int max) {
            if (min > max) {
                throw new IllegalArgumentException("Parameter `min` is larger than parameter `max`");
            }

            this.min = min;
            this.max = max;
        }

        public boolean contains(int value) {
            return min <= value && value <= max;
        }
    }

    public static int bytecodeVersionForJava(int javaVersion) {
        return javaVersion + 44;
    }

    /**
     * Return the allowed version range for a given class.
     * @param rpmPackage RpmPackage.
     * @param jarName The full path to the .jar archive that is being inspected.
     * @param className The full relative path to the .class file inside the .jar archive.
     * @return Allowed version range for @param className.
     */
    VersionRange versionRangeOf(RpmPackage rpm, String jarName, String className);
}
