#!/bin/sh
set -eu

if [ -z "${JAVA_HOME:-}" ]; then
    JAVA_HOME=$(cat target/classes/JAVA_HOME)
fi

exec "${JAVA_HOME}"/bin/java --enable-native-access ALL-UNNAMED -cp "target/classes:$(cat target/classpath)" org.fedoraproject.javapackages.validator.Main "${@}"
