#!/bin/bash
set -eux

jp_validator_image='javapackages-validator:2'
test_artifacts_dir='/tmp/test_artifacts'
jpv_tests_dir='/tmp/javapackages.git'
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
    git clone --mirror "${jpv_tests_url}" "${jpv_tests_dir}"
}

execute() {
    mkdir -p /tmp/jpv-classes
    find /tmp/jpv-classes -exec touch -m -d '9999-01-01 00:00:00' {} +

    for component in "${test_artifacts_dir}"/rpms/*; do
        component="${component##*/}"
        run_id="jpv-ci-${component}"
        echo "::group::Run tests for ${component}"

        # Discover tests
        if ! tmt -q --root "${test_artifacts_dir}/rpms/${component}" \
             run --scratch \
                 --id "${run_id}" \
             discover --how fmf \
                      --url "${jpv_tests_dir}" \
                      --ref "${jpv_tests_ref}" \
             plan --name /plans/javapackages \
             test --filter tag:-runit
        then
            echo "::endgroup::"
            continue
        fi

        # Execute tests
        if tmt -vvv \
           run --id "${run_id}" \
               -e TEST_ARTIFACTS="${test_artifacts_dir}/rpms/${component}/rpms" \
               -e JP_VALIDATOR_IMAGE="${jp_validator_image}" \
               -e JP_VALIDATOR_OUTPUT_DIR="/tmp/jpv-classes" \
           provision --how local \
           execute --how tmt --no-progress-bar
        then
            echo "::endgroup::"
            continue
        fi

        # Display test report if testing failed
        tmt -vvv run --id "${run_id}" report
    done
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
