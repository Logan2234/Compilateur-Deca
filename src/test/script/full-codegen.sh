#!/bin/bash

# This script executes test_codegen on all <example-name>.deca files in src/test/codegen.
# The outputs are saved in files named <example-name>-codegen.lis to differentiate them
# from the output files of full-lex.sh which is executed on the same files.

# Change the current working directory to be in the project's directory
# wherever the script is executed from
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

#### valid tests ####
files=$(find ./src/test/deca/codegen/valid -name "*.deca")

for test in $files
do
    # save the output
    test_codegen "$test" > "${test%.deca}"-codegen.lis 2>&1

    # check if passed
    if cat "${test%.deca}"-codegen.lis | grep -q "$test\|java:"
    then
        echo "Error detected on a valid test $test"
        # exit 1
    else
        echo "Test passed $test"
    fi
done

#### invalid tests ####
files=$(find ./src/test/deca/codegen/invalid -name "*.deca")

for test in $files
do
    # save the output
    test_codegen "$test" > "${test%.deca}"-codegen.lis 2>&1

    # check if passed
    if cat "${test%.deca}"-codegen.lis | grep -q "$test\|java:"
    then
        echo "Test passed $test"
    else
        echo "No error detected on an invalid test $test"
        # exit 1
    fi
done