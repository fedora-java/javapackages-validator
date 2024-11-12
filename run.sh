#!/bin/sh
set -eu

if [ -z "${JAVA_HOME:-}" ]; then
    JAVA_HOME=$(cat target/classes/JAVA_HOME)
fi

exec "${JAVA_HOME}"/bin/java --enable-native-access ALL-UNNAMED -jar 'target/validator.jar' "${@}"

# exec "${JAVA_HOME}"/bin/java --enable-native-access ALL-UNNAMED -cp "target/classes$(find target/dependency -type f -printf ':%p')" org.fedoraproject.javapackages.validator.Main "${@}"
