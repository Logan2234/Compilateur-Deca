#!/bin/bash

# This script executes test_codegen on all <example-name>.deca files in src/test/codegen.
# The outputs are saved in files named <example-name>-codegen.lis to differentiate them
# from the output files of full-lex.sh which is executed on the same files.

# Change the current working directory to be in the project's directory
# wherever the script is executed from
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

#### valid tests ####
files=$(find ./src/test/deca/context/valid -maxdepth 1 -name "*.deca")

for test in $files
do
    decac -p "$test"

    # check if passed
    if [[ $? -eq 1 ]]
    then
        echo "Error detected on a valid test $test"
        # exit 1
    else
        echo "Test passed $test"
    fi
done
