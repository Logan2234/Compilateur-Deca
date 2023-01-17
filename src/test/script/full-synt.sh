#!/bin/bash

# This script executes test_synt on all <example-name>.deca files in src/test/syntax.
# The outputs are saved in files named <example-name>-synt.lis to differentiate them
# from the output files of full-lex.sh which is executed on the same files.

# Change the current working directory to be in the project's directory
# wherever the script is executed from
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

BRED='\033[1;31m'
RED='\033[0;31m'
BGREEN='\033[1;32m'
GREEN='\033[0;32m'
BWHITE='\033[1;37m'
NOCOLOR='\033[0m'

# Stat var
NB_VALID_TESTS=0
NB_INVALID_TESTS=0
VALID_PASSED=0
INVALID_PASSED=0

echo -e "$BWHITE\n============================= Valid syntax tests =============================\n"

files=$(find ./src/test/deca/syntax/valid/provided -name "*.deca")
files+=" "
files+=$(find ./src/test/deca/syntax/valid/synt -name "*.deca")

for test in $files
do
    ((NB_VALID_TESTS = NB_VALID_TESTS + 1))

    # save the output
    test_synt "$test" > /dev/null 2>&1

    # check if passed
    if [ $? -ne 0 ]
    then
        echo -e "${BRED}Test failed ($VALID_PASSED/$NB_VALID_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${RED} returns an error"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
    else
        ((VALID_PASSED = VALID_PASSED + 1))
        echo -e "${BGREEN}Test passed ($VALID_PASSED/$NB_VALID_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${GREEN} returns no error"
    fi
done

echo -e "$BWHITE\n============================= Invalid syntax tests =============================\n"
# an invalid test (syntaxically) may be lexically correct
# a file named *_lex.deca is lexically correct

files=$(find ./src/test/deca/syntax/invalid/provided -name "*.deca")
files+=" "
files+=$(find ./src/test/deca/syntax/invalid/synt -name "*.deca")

for test in $files
do
    ((NB_INVALID_TESTS = NB_INVALID_TESTS + 1))

    # check if passed
    if test_synt "$test" 2>&1 | grep -q "$test\|java:\|mismatched"
    then
        ((INVALID_PASSED = INVALID_PASSED + 1))
        echo -e "${BGREEN}Test passed ($INVALID_PASSED/$NB_INVALID_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${GREEN} returns a correct error"
    else
        echo -e "${BRED}Test failed ($INVALID_PASSED/$NB_INVALID_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${RED} returns no error"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
    fi
done

VALID_PASSED_PERCENTAGE=`echo "$VALID_PASSED / $NB_VALID_TESTS * 100" | bc -l`
INVALID_PASSED_PERCENTAGE=`echo "$INVALID_PASSED / $NB_INVALID_TESTS * 100" | bc -l`

TEMP=`echo "$VALID_PASSED_PERCENTAGE > 0.5" | bc -l`

if ((TEMP));
then
    echo -e "\n${GREEN} Valid test passed: "
    printf %2.0f $VALID_PASSED_PERCENTAGE
    echo "%"
else
    echo -e "\n${RED} Valid test passed: "
    printf %2.0f $VALID_PASSED_PERCENTAGE
    echo "%"
fi

TEMP=`echo "$INVALID_PASSED_PERCENTAGE > 0.5" | bc -l`
if ((TEMP));
then
    echo -e "\n${GREEN} Invalid test passed: "
    printf %2.0f $INVALID_PASSED_PERCENTAGE
    echo -e "%\n"
else
    echo -e "\n${RED} Invalid test passed: "
    printf %2.0f $INVALID_PASSED_PERCENTAGE
    echo -e "%\n"
fi
