#!/bin/bash

set -eu

component="${1}"
release="${2:-rawhide}"

nevra="$(koji latest-pkg "${release}" "${component}" | tail -n +3)"
nevra="${nevra%%[[:space:]]*}"

koji download-build --noprogress --arch noarch --arch x86_64 --arch src --debuginfo "${nevra}"
