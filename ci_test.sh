#!/bin/bash

set -ex

jp_validator_image='javapackages-validator'
test_artifacts_dir='/opt/test_artifacts'
jpv_tests_dir='/opt/javapackages-validator-tests'

build_local_image() {
    podman build -f Dockerfile.main -t "${jp_validator_image}"
}

download_ci_env() {
    mkdir -p "${test_artifacts_dir}"
    podman run --privileged --mount type=bind,source="${test_artifacts_dir}",target='/mnt/rpms' --rm -it "quay.io/mizdebsk/javapackages-validator-ci" '/mnt/rpms'
}

prepare_test_env() {
    download_ci_env
    git -C "${jpv_tests_dir%/*}" clone 'https://pagure.io/javapackages-validator-tests.git'
    git -C "${jpv_tests_dir}" checkout '9e4c944408ff77d46125a1f73ad926e8896fdf77'
    echo "
package org.fedoraproject.javapackages.validator.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SymlinkConfigF37 extends SymlinkConfig.RpmSet {
    public SymlinkConfigF37() throws IOException {
        super(Files.find(Paths.get(\"/mnt/test_artifacts\"), Integer.MAX_VALUE,
                (path, attributes) -> !attributes.isDirectory() && path.toString().endsWith(\".rpm\")).iterator());
    }
}
" > "${jpv_tests_dir}/config/src/org/fedoraproject/javapackages/validator/config/SymlinkConfigF37.java"
}

execute() {
    ls -l "${jpv_tests_dir}"
    cd "${jpv_tests_dir}/test_scripts"
    JP_VALIDATOR_IMAGE="${jp_validator_image}" TEST_ARTIFACTS="${test_artifacts_dir}" bash -x ./jp_validator.sh -r -x 'All'
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
