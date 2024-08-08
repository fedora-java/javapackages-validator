#!/bin/sh
set -eu

: ${JAVA_HOME:=/usr/lib/jvm/jre-22}

exec "${JAVA_HOME}"/bin/java --enable-native-access ALL-UNNAMED -jar 'target/validator.jar' "${@}"

# exec "${JAVA_HOME}"/bin/java --enable-native-access ALL-UNNAMED -cp "target/classes$(find target/dependency -type f -printf ':%p')" org.fedoraproject.javapackages.validator.Main "${@}"
