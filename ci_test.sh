#!/bin/bash

set -ex

jp_validator_image='javapackages-validator'
test_artifacts_dir='/tmp/test_artifacts'
jpv_tests_dir='/tmp/javapackages-validator-tests.git'
jpv_tests_url='https://pagure.io/javapackages-validator-tests'
jpv_tests_ref='17acc96ee933e080640f244918bd12e74b92aae2'

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
    git clone --mirror "${jpv_tests_url}" "${jpv_tests_dir}"
}

execute() {
    for component in $(ls ${test_artifacts_dir}/rpms); do
        if [ -f "${test_artifacts_dir}/rpms/${component}/plans/javapackages.fmf" ]; then
            tmt --root "${test_artifacts_dir}/rpms/${component}" run \
                -e TEST_ARTIFACTS="${test_artifacts_dir}/rpms/${component}"\
                -e JP_VALIDATOR_IMAGE="${jp_validator_image}"\
                -e ENVROOT="${test_artifacts_dir}/envroot"\
                discover --how fmf --url "${jpv_tests_dir}" --ref "${jpv_tests_ref}"\
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
