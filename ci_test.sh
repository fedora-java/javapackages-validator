#!/bin/sh
set -eu

echo Environment:
echo JP_VALIDATOR_IMAGE=${JP_VALIDATOR_IMAGE:=javapackages-validator:2}
echo JP_VALIDATOR_OUTPUT_DIR=${JP_VALIDATOR_OUTPUT_DIR:=}
echo TEST_ARTIFACTS=${TEST_ARTIFACTS:=/tmp/test-artifacts}
echo JPV_CI_IMAGE=${JPV_CI_IMAGE:=quay.io/fedora-java/javapackages-validator-ci:2}
echo JPV_CI_TESTS_URL=${JPV_CI_TESTS_URL:=https://src.fedoraproject.org/tests/javapackages.git}
echo JPV_CI_TESTS_REF=${JPV_CI_TESTS_REF:=main}

set -x

build_local_image() {
    podman build -f Dockerfile.main -t "${JP_VALIDATOR_IMAGE}"
}

download_ci_env() {
    mkdir -p "${TEST_ARTIFACTS}"
    podman run --privileged --mount type=bind,source="${TEST_ARTIFACTS}",target='/mnt/rpms' --rm -it "${JPV_CI_IMAGE}" '/mnt/rpms'
}

prepare_test_env() {
    download_ci_env
}

execute() {
    tmt --version
    tmt -vvv \
        --feeling-safe \
        run --scratch \
            --id "jpv-ci" \
            -e TEST_ARTIFACTS="${TEST_ARTIFACTS}" \
            -e JP_VALIDATOR_IMAGE="${JP_VALIDATOR_IMAGE}" \
            -e JP_VALIDATOR_OUTPUT_DIR="${JP_VALIDATOR_OUTPUT_DIR}" \
        discover --how fmf \
                 --url "${JPV_CI_TESTS_URL}" \
                 --ref "${JPV_CI_TESTS_REF}" \
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
