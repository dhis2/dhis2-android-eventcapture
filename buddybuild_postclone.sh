#!/usr/bin/env bash

# Definitions
gitPath=$(git rev-parse --show-toplevel)

# Generate last commit
sh ${gitPath}/generate_last_commit.sh

# Use event capture SDK branch
cd sdk

git checkout 2.22-legacy
cd -

echo "Generate Test Coverage Report:"
./gradlew build jacocoTestReport assembleAndroidTest
