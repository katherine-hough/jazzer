set -e

docker build --pull -t cifuzz/jazzer "$@" docker/jazzer
docker build -t cifuzz/jazzer-autofuzz "$@" docker/jazzer-autofuzz
