#!/bin/bash

# This script executes test_synt on all <example-name>.deca files in src/test/syntax.
# The outputs are saved in files named <example-name>-synt.lis to differentiate them
# from the output files of full-lex.sh which is executed on the same files.

# Change the current working directory to be in the project's directory
# wherever the script is executed from
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

echo "#################### valid tests ####################"
files=$(find ./src/test/deca/syntax/valid/provided -name "*.deca")
files+=" "
files+=$(find ./src/test/deca/syntax/valid/synt -name "*.deca")

for test in $files
do
    # save the output
    test_synt "$test" > "${test%.deca}"-synt.lis 2>&1

    # check if passed
    if cat "${test%.deca}"-synt.lis | grep -q "$test\|java:"
    then
        echo "Error detected on a valid test $test"
        exit 1
    else
        echo "Test passed $test"
    fi
done

echo "#################### invalid tests ####################"
# an invalid test (syntaxically) may be lexically correct
# a file named *_lex.deca is lexically correct
files=$(find ./src/test/deca/syntax/invalid/provided -name "*.deca")
files+=" "
files+=$(find ./src/test/deca/syntax/invalid/synt -name "*.deca")

for test in $files
do
    # save the output
    test_synt "$test" > "${test%.deca}"-synt.lis 2>&1

    # check if passed
    if cat "${test%.deca}"-synt.lis | grep -q "$test\|java:"
    then
        echo "Test passed $test"
    else
        echo "No error detected on an invalid test $test"
        exit 1
    fi
done