#!/bin/bash

set -eu

component="${1}"
release="${2:-rawhide}"

nevra="$(koji latest-pkg "${release}" "${component}" | tail -n +3)"
nevra="${nevra%%[[:space:]]*}"
echo "${nevra}"
dist_git_url="$(koji buildinfo "${nevra}" | grep Extra | sed "s/.*git+\(.*\)'.*/\1/")"
echo "${dist_git_url}"
git clone "${dist_git_url%%#*}"
cd "${component}"
git checkout -q "${dist_git_url##*#}"

mkdir rpms
pushd rpms
koji download-build --arch noarch --arch x86_64 --arch src --debuginfo "${nevra}"
popd
