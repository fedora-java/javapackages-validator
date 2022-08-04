#!/bin/bash

set -e

java_bin='/usr/lib/jvm/jre-17/bin'

classpath="$(echo target/*.jar)"
for dependency in target/dependency/*; do
    classpath+=":${dependency}"
done

exec ${java_bin}/java --enable-preview --add-modules jdk.incubator.foreign --enable-native-access ALL-UNNAMED -cp "${classpath}" 'org.fedoraproject.javapackages.validator.Main' ${@}
