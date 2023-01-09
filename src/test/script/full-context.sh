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

clear

echo -e "${GREENBOLD} \n============================= Testing valid tests =============================\n ${NOCOLOR}"

files=$(find ./src/test/deca/context/valid -name "*.deca")

for test in $files
do
    test_context "$test" > "${test%.deca}".lis 2>&1
        ((NB_VALID_TESTS = NB_VALID_TESTS + 1))
    if cat "${test%.deca}".lis | grep -q "Exception in thread"
    then
        echo -e "${REDBOLD}Error detected on a valid test: ${RED}$test${NOCOLOR}"
    else
        echo -e "${GREENBOLD}Test passed: ${GREEN}$test${NOCOLOR}"
        ((VALID_PASSED = VALID_PASSED + 1))
    fi
done

echo -e "${GREENBOLD} \n============================= Testing invalid tests =============================\n ${NOCOLOR}"

files=$(find ./src/test/deca/context/invalid -name "*.deca")

for test in $files
do
    ((NB_INVALID_TESTS = NB_INVALID_TESTS + 1))
    test_context "$test" > "${test%.deca}".lis 2>&1
    if cat "${test%.deca}".lis | grep -q "$test:*:*"
    then
        echo -e "${GREENBOLD}Test passed: ${GREEN}$test${NOCOLOR}"
        ((INVALID_PASSED = INVALID_PASSED + 1))
    else
        echo -e "${REDBOLD}Error not detected on an invalid test: ${RED}$test${NOCOLOR}"
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
    echo "%"
fi