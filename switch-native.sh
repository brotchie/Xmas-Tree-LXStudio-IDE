#!/bin/bash

set -eu

readonly LXSTUDIO_PATH=$(dirname $(realpath $0))
readonly PROCESSING_PATH="${LXSTUDIO_PATH}/lib/processing-4.0.1"
readonly NATIVE_SYMLINK_PATH="${PROCESSING_PATH}/native"

function usage() {
	echo "Usage: switch-native.sh [arch]"
	echo 
	echo "Switches LXStudio processing native architecture to arch."
	echo
	echo "Available architectures:"
	echo
	tree -d -L 1 "${PROCESSING_PATH}"
}


if [ $# -ne 1 ]; then
	usage
	exit 1
else
	readonly DIR_TO_LINK="${PROCESSING_PATH}/$1"
	if [ ! -d "${DIR_TO_LINK}" ]; then
		echo "No directory $1 in ${PROCESSING_PATH}, aborting."
		echo
		usage
		exit 1
	fi

	if [ -L "${NATIVE_SYMLINK_PATH}" ]; then
		echo "Deleting existing native symlink."
		rm "${NATIVE_SYMLINK_PATH}"
	fi

	echo "Switched LXStudio native library to $1 architecture."
	(cd "${PROCESSING_PATH}" && ln -s $1 native)
fi
