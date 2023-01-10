#!/bin/bash

# Colors:
RED='\033[0;31m'
REDBOLD='\033[0;31;1m'
GREENBOLD='\033[0;32;1m'
GREEN='\033[0;32m'
NOCOLOR='\033[0m'

# Stat var
NB_VALID_TESTS=0
NB_INVALID_TESTS=0
VALID_PASSED=0
INVALID_PASSED=0

# On se place dans le répertoire du projet (quel que soit le
# répertoire d'où est lancé le script) :
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

echo -e "${GREENBOLD} \n============================= Testing context on valid tests =============================\n ${NOCOLOR}"

files=$(find ./src/test/deca/context/valid -maxdepth 1 -name "*.deca")

for test in $files
do
    test_context "$test" > "${test%.deca}".lis 2>&1
        ((NB_VALID_TESTS = NB_VALID_TESTS + 1))
    if cat "${test%.deca}".lis | grep -q "${test}\|Exception in thread"
    then
        echo -e "${REDBOLD}Error detected on a valid test ($VALID_PASSED/$NB_VALID_TESTS): ${RED}$test${NOCOLOR}"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
    else
        ((VALID_PASSED = VALID_PASSED + 1))
        echo -e "${GREENBOLD}Test passed ($VALID_PASSED/$NB_VALID_TESTS): ${GREEN}$test${NOCOLOR}"
    fi
done

echo -e "${GREENBOLD} \n============================= Testing context on invalid tests =============================\n ${NOCOLOR}"

files=$(find ./src/test/deca/context/invalid -maxdepth 1 -name "*.deca")

for test in $files
do
    ((NB_INVALID_TESTS = NB_INVALID_TESTS + 1))
    test_context "$test" > "${test%.deca}".lis 2>&1
    if cat "${test%.deca}".lis | grep -q "$test:*:*"
    then
        ((INVALID_PASSED = INVALID_PASSED + 1))
        echo -e "${GREENBOLD}Test passed ($INVALID_PASSED/$NB_INVALID_TESTS): ${GREEN}$test${NOCOLOR}"
    else
        echo -e "${REDBOLD}Error not detected on an invalid test ($INVALID_PASSED/$NB_INVALID_TESTS): ${RED}$test${NOCOLOR}"
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
    printf %2.2f $VALID_PASSED_PERCENTAGE
    echo "%"
else
    echo -e "\n${RED} Valid test passed: "
    printf %2.2f $VALID_PASSED_PERCENTAGE
    echo "%"
fi

TEMP=`echo "$INVALID_PASSED_PERCENTAGE > 0.5" | bc -l`
if ((TEMP));
then
    echo -e "\n${GREEN} Invalid test passed: "
    printf %2.2f $INVALID_PASSED_PERCENTAGE
    echo "%"
else
    echo -e "\n${RED} Invalid test passed: "
    printf %2.2f $INVALID_PASSED_PERCENTAGE
    echo -e "%\n\n"
fi