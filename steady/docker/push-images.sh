#!/bin/bash
# Push steady images to a registry
#
# Usage: push.sh -r <registry> -p <project> -v <steady-version>
#
# To run this script you should have already generated the JARs and be logged in the registry
# Use `docker login` to login to the registry
# Read more here: https://eclipse.github.io/steady/admin

RELEASE_PATTERN="([0-9]+\.[0-9]+\.[0-9]+(.*)?)|latest"

set -e

usage () {
    cat <<HELP_USAGE
Usage: $0 [options...]
 -r, --registry <registry>  The Docker registry (e.g., docker.io)
 -p, --project <project>    The project or user where to nest it (e.g., eclipse)
 -v, --version <version>    The version of the images to push (e.g., 3.1.5)
 -h, --help                 This help text
HELP_USAGE
    exit 0
}

if ! options=$(getopt -o r:p:v:h -l registry:,project:,version:,help -- "$@")
then
    usage
    exit 1
fi

#set -- $options

while true; do
    case "$1" in
        -r | --registry ) REGISTRY="$2"; shift 2 ;;
        -p | --project ) PROJECT="$2"; shift 2 ;;
        -v | --version ) VULAS_RELEASE="$2"; shift 2 ;;
        -h | --help ) usage; shift 1 ;;
        -- ) shift; break ;;
        * ) break ;;
    esac
done

if [[ -z "${VULAS_RELEASE// }" ]]; then
    usage
    exit 1
fi

REGISTRY=${REGISTRY//\'/}
PROJECT=${PROJECT//\'/}
VULAS_RELEASE=${VULAS_RELEASE//\'/}

if [[ ! $VULAS_RELEASE =~ $RELEASE_PATTERN ]]; then
    echo '[-] You did not specify a valid pattern for the release. Use -r switch. The format is: \d\.\d\.\d(.*)?'
    usage
    exit 1
fi

SERVICES='frontend-apps frontend-bugs patch-lib-analyzer rest-backend rest-lib-utils kb-importer'

VULAS_RELEASE=${VULAS_RELEASE} docker-compose -f docker-compose.build.yml build

if [[ "$(docker images -q steady-rest-backend:"$VULAS_RELEASE" 2> /dev/null)" == "" ]]; then
    echo "[-] There are no local images for release $VULAS_RELEASE"
    exit 1
fi

for service in $SERVICES ; do
    IMAGE=${REGISTRY}/${PROJECT}/steady-${service}
    docker tag steady-"${service}":"${VULAS_RELEASE}" "${IMAGE}:${VULAS_RELEASE}"
    docker push "${IMAGE}:${VULAS_RELEASE}"

    docker tag steady-"${service}":"${VULAS_RELEASE}" "${IMAGE}:latest"
    docker push "${IMAGE}:latest"
done
