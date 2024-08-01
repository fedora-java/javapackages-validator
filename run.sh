#!/bin/sh
set -eu

: ${JAVA_HOME:=/usr/lib/jvm/jre-22}

exec "${JAVA_HOME}"/bin/java --enable-native-access ALL-UNNAMED -jar 'target/validator.jar' "${@}"
