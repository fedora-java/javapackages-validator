#!/bin/sh
set -e

if [ -z "${JAVA_HOME}" ]; then
	for path in $(find /usr/lib/jvm -mindepth 1 -maxdepth 1 -type d); do
		if [ "$(echo "${path}" | sed -E 's%/usr/lib/jvm/(java|jre)-([0-9]*).*%\2%')" -ge 22 ]; then
			JAVA_HOME="${path}"
			break
		fi
	done
fi

exec "${JAVA_HOME}"/bin/java --enable-native-access ALL-UNNAMED -jar 'target/validator.jar' "${@}"

# exec "${JAVA_HOME}"/bin/java --enable-native-access ALL-UNNAMED -cp "target/classes$(find target/dependency -type f -printf ':%p')" org.fedoraproject.javapackages.validator.Main "${@}"
