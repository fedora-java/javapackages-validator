#!/bin/bash

set -ex

jp_validator_image='javapackages-validator'
test_artifacts_dir='/tmp/test_artifacts'
jpv_tests_dir='/tmp/javapackages-validator-tests'
jpv_tests_url='https://pagure.io/javapackages-validator-tests'

build_local_image() {
    # Add colored and debug outputs
    sed -i 's|ENTRYPOINT \["./run.sh"\]|ENTRYPOINT ["./run.sh", "-r", "-x"]|' Dockerfile.main
    podman build -f Dockerfile.main -t "${jp_validator_image}"
}

download_ci_env() {
    mkdir -p "${test_artifacts_dir}"
    podman run --privileged --mount type=bind,source="${test_artifacts_dir}",target='/mnt/rpms' --rm -it "quay.io/mizdebsk/javapackages-validator-ci" '/mnt/rpms'
}

prepare_test_env() {
    download_ci_env
    git -C "${jpv_tests_dir%/*}" clone "${jpv_tests_url}.git"
    git -C "${jpv_tests_dir}" checkout 'cc9538d898bd000ceeef53d8cb4009701ad172dd'
    find "${test_artifacts_dir}" -mindepth 3 -maxdepth 3 -wholename '*/*/plans/javapackages.fmf' -exec sed -i "s|url: ${jpv_tests_url}|path: ${jpv_tests_dir}|" {} +
}

execute_symlink_check() {
    echo "
package org.fedoraproject.javapackages.validator.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SymlinkConfigJP extends SymlinkConfig.RpmSetImpl {
    public SymlinkConfigJP() throws IOException {
        super(Files.find(Paths.get(\"/mnt/test_artifacts\"), Integer.MAX_VALUE,
                (path, attributes) -> !attributes.isDirectory() && path.toString().endsWith(\".rpm\")).iterator());
    }
}
" > "${jpv_tests_dir}/src/SymlinkConfigJP.java"
    pushd "${jpv_tests_dir}"
    TEST_ARTIFACTS="${test_artifacts_dir}" JP_VALIDATOR_IMAGE="${jp_validator_image}" ./jp_validator.sh -r -x SymlinkCheck -c /mnt/config/SymlinkConfigJP.java
    popd
    echo "public class SymlinkConfigJP {}" > "${jpv_tests_dir}/src/SymlinkConfigJP.java"
}

execute() {
    execute_symlink_check
    
    for component in $(ls ${test_artifacts_dir}); do
        if [ -f "${test_artifacts_dir}/${component}/plans/javapackages.fmf" ]; then
            tmt --root "${test_artifacts_dir}/${component}" run \
                -e TEST_ARTIFACTS="${test_artifacts_dir}/${component}"\
                -e JP_VALIDATOR_IMAGE="${jp_validator_image}"\
                discover --how fmf --path "${jpv_tests_dir}"\
                provision --how local\
                execute\
                report -vvv\
                plan --name /plans/javapackages
        fi
    done
}

if [ "${1}" = 'build_local_image' ]; then
    build_local_image
elif [ "${1}" = 'prepare' ]; then
    prepare_test_env
elif [ "${1}" = 'execute' ]; then
    execute
else
    echo "Unrecognized option: ${1}"
    exit 1
fi
