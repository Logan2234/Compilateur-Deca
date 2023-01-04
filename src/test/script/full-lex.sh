#!/bin/bash

# On se place dans le répertoire du projet (quel que soit le
# répertoire d'où est lancé le script) :
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

# valid tests
files=$(find ./src/test/deca/syntax/valid -name "*.deca")


for test in $files
do
    # save the output
    test_lex "$test" > "${test%.deca}".lis 2>&1

    # check if passed
    if cat "${test%.deca}".lis | tail -n 1 | grep -q "$test"
    then
        echo "Error detected on a valid test $test"
        exit 1
    else
        echo "Test passed $test"
    fi
done

# invalid tests that may be lexically correct
# a file named *_lex.deca is lexically correct
files=$(find ./src/test/deca/syntax/invalid -name "*.deca")

for test in $files
do
    # save the output
    test_lex "$test" > "${test%.deca}".lis 2>&1

    if [[ $test =~ .*\_lex.deca ]]
    then
        # the program is lexically correct
        if cat "${test%.deca}".lis | tail -n 1 | grep -q "$test"
        then
            echo "Error detected on a valid test $test"
            exit 1
        else
            echo "Test passed $test"
        fi
    else
        # the program is lexically incorrect
        if cat "${test%.deca}".lis | tail -n 1 | grep -q "$test"
        then
            echo "Test passed $test"
        else
            echo "No error detected on an invalid test $test"
            exit 1
        fi
    fi
done