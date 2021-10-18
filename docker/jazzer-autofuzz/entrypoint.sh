#!/usr/bin/env sh

CP="$(/app/coursier.jar fetch --classpath "$1")"
/app/jazzer_driver \
  -artifact_prefix=/fuzzing/ \
  --reproducer_path=/fuzzing \
  --cp="$CP" \
  --autofuzz="$2"
