#!/bin/bash

# This script executes test_lex on all <example-name>.deca files in src/test/syntax.
# The outputs are saved in files named <example-name>-lex.lis to differentiate them
# from the output files of full-synt.sh which is executed on the same files.

# A file named *_lex.deca denotes an example that is syntaxically invalid but
# lexically correct.

# Change the current working directory to be in the project's directory
# wherever the script is executed from
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

echo "#################### valid tests ####################"
files=$(find ./src/test/deca/syntax/valid/provided -name "*.deca")

for test in $files
do
    # save the output
    test_lex "$test" > "${test%.deca}"-lex.lis 2>&1

    # check if passed
    if cat "${test%.deca}"-lex.lis | grep -q "$test\|DUMMY_TOKEN:\|WS"
    then
        echo "Error detected on a valid test $test"
        # exit 1
    else
        echo "Test passed $test"
    fi
done

echo "#################### invalid tests ####################"
# an invalid test (syntaxically) may be lexically correct
# a file named *_lex.deca is lexically correct
files=$(find ./src/test/deca/syntax/invalid/provided -name "*.deca")
files+=" "
files+=$(find ./src/test/deca/syntax/invalid/lexer -name "*.deca")

for test in $files
do
    # save the output
    test_lex "$test" > "${test%.deca}"-lex.lis 2>&1

    if [[ $test =~ .*\_lex.deca ]]
    then
        # the program is lexically correct
        if cat "${test%.deca}"-lex.lis | grep -q "$test\|DUMMY_TOKEN:\|WS"
        then
            echo "Error detected on a valid test $test"
            # exit 1
        else
            echo "Test passed $test"
        fi
    else
        # the program is lexically incorrect
        if cat "${test%.deca}"-lex.lis | grep -q "$test\|DUMMY_TOKEN:\|WS"
        then
            echo "Test passed $test"
        else
            echo "No error detected on an invalid test $test"
            # exit 1
        fi
    fi
done
