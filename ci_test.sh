#!/bin/bash
set -eu

jp_validator_image='javapackages-validator:1'
test_artifacts_dir='/tmp/test_artifacts'
jpv_tests_dir='/tmp/javapackages-tests.git'
jpv_tests_url='https://src.fedoraproject.org/tests/javapackages.git'
jpv_tests_ref='f37'

build_local_image() {
    podman build -f Dockerfile.main -t "${jp_validator_image}"
}

download_ci_env() {
    mkdir -p "${test_artifacts_dir}"
    podman run --privileged --mount type=bind,source="${test_artifacts_dir}",target='/mnt/rpms' --rm -it "quay.io/fedora-java/javapackages-validator-ci:1" '/mnt/rpms'
}

prepare_test_env() {
    download_ci_env
    git clone --mirror "${jpv_tests_url}" "${jpv_tests_dir}"
}

execute() {
    for component in $(ls ${test_artifacts_dir}/rpms); do
        run_id="jpv-ci-${component}"
        echo "::group::Run tests for ${component}"

        # Discover tests
        if ! tmt -q --root "${test_artifacts_dir}/rpms/${component}" \
             run --scratch \
                 --id "${run_id}" \
             discover --how fmf \
                      --url "${jpv_tests_dir}" \
                      --ref "${jpv_tests_ref}" \
             plan --name /plans/javapackages
        then
            echo "::endgroup::"
            continue
        fi

        # Execute tests
        if tmt -q \
           run --id "${run_id}" \
               -e TEST_ARTIFACTS="${test_artifacts_dir}/rpms/${component}/rpms" \
               -e JP_VALIDATOR_IMAGE="${jp_validator_image}" \
               -e ENVROOT="${test_artifacts_dir}/envroot" \
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
