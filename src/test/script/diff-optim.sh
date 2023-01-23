#!/bin/bash

# Colors:
RED='\033[0;31m'
REDBOLD='\033[0;31;1m'
GREENBOLD='\033[0;32;1m'
GREEN='\033[0;32m'
NOCOLOR='\033[0m'
BWHITE='\033[1;37m'

# Stat var
NB_VALID_TESTS=0
VALID_PASSED=0

# On se place dans le répertoire du projet (quel que soit le
# répertoire d'où est lancé le script) :
cd "$(dirname "$0")"/../../../ || exit 1

PATH=./src/test/script/launchers:"$PATH"

echo -e "${BWHITE} \n============================= Optim test and decompilation =============================\n"

files=$(find ./src/test/deca/optim -maxdepth 2 -name "*.deca")

for test in $files
do
    ((NB_VALID_TESTS = NB_VALID_TESTS + 1))
    #test_context "$test" > /dev/null 2>&1
    test_optim "$test" > "${test%.deca}".lis 2>&1
    decac -o -p "$test" > "${test%.deca}".decomp
    
    if [ $? -ne 0 ]
    then
        echo -e "${REDBOLD}Test failed ($VALID_PASSED/$NB_VALID_TESTS): ${RED}$test${NOCOLOR}"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
    else
        ((VALID_PASSED = VALID_PASSED + 1))
        echo -e "${GREENBOLD}Test passed ($VALID_PASSED/$NB_VALID_TESTS): ${GREEN}$test${NOCOLOR}"
    fi
done


VALID_PASSED_PERCENTAGE=`echo "$VALID_PASSED / $NB_VALID_TESTS * 100" | bc -l`
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