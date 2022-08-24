#!/bin/bash

set -e

java_bin='/usr/lib/jvm/jre-17/bin'

exec "${java_bin}"/java --enable-preview --add-modules jdk.incubator.foreign --enable-native-access ALL-UNNAMED -jar 'target/validator.jar' ${@}
