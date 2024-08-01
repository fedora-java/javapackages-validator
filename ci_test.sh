#!/bin/bash
set -eux

jp_validator_image='javapackages-validator:2'
test_artifacts_dir='/tmp/test_artifacts'
jpv_tests_url='https://src.fedoraproject.org/tests/javapackages.git'
jpv_tests_ref='main'

build_local_image() {
    podman build -f Dockerfile.main -t "${jp_validator_image}"
}

download_ci_env() {
    mkdir -p "${test_artifacts_dir}"
    podman run --privileged --mount type=bind,source="${test_artifacts_dir}",target='/mnt/rpms' --rm -it "quay.io/fedora-java/javapackages-validator-ci:2" '/mnt/rpms'
}

prepare_test_env() {
    download_ci_env
}

execute() {
    mkdir -p /tmp/jpv-classes
    find /tmp/jpv-classes -exec touch -m -d '9999-01-01 00:00:00' {} +

    tmt -vvv \
        run --scratch \
            --id "jpv-ci" \
            -e TEST_ARTIFACTS="${test_artifacts_dir}" \
            -e JP_VALIDATOR_IMAGE="${jp_validator_image}" \
            -e JP_VALIDATOR_OUTPUT_DIR="/tmp/jpv-classes" \
        discover --how fmf \
                 --url "${jpv_tests_url}" \
                 --ref "${jpv_tests_ref}" \
        provision --how local \
        execute --how tmt \
                --no-progress-bar \
        report
}

if [ "${1}" = 'build-local-image' ]; then
    build_local_image
elif [ "${1}" = 'prepare' ]; then
    prepare_test_env
elif [ "${1}" = 'execute' ]; then
    execute
else
    echo "Unrecognized option: ${1}"
    exit 1
fi
