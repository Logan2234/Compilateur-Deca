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
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

echo -e "${BWHITE} \n============================= Valid tests =============================\n"

files=$(find ./src/test/deca/codegen/valid -maxdepth 1 -name "*.deca")

for test in $files
do
    ((NB_VALID_TESTS = NB_VALID_TESTS + 1))
    if decac "$test" 2>&1 | grep -q "${test}\|Exception in thread" # TODO: Le test a faire est plutot un diff avec un fichier res
    then
        echo -e "${REDBOLD}Test failed ($VALID_PASSED/$NB_VALID_TESTS): ${NOCOLOR}$test${RED} compilation failed"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
    else
        ima "${test%deca}"ass > "${test%.deca}"-temp.res 
        diff=$(diff ""${test%deca}"res" "${test%.deca}-temp.res")
        if [ "$diff" = "" ]
        then
            ((VALID_PASSED = VALID_PASSED + 1))
            echo -e "${GREENBOLD}Test passed ($VALID_PASSED/$NB_VALID_TESTS): ${NOCOLOR}$test$GREEN same result as in the correction"
        else
        echo -e "${REDBOLD}Test failed ($VALID_PASSED/$NB_VALID_TESTS): ${NOCOLOR}$test${RED} result is not the same as in the correction"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
        fi
    fi
done

files=$(find ./src/test/deca/codegen/interactive -maxdepth 1 -name "*.deca")

for test in $files
do
    ((NB_VALID_TESTS = NB_VALID_TESTS + 1))
    if decac "$test" 2>&1 | grep -q "${test}\|Exception in thread"
    then
        echo -e "${REDBOLD}Test failed ($VALID_PASSED/$NB_VALID_TESTS): ${NOCOLOR}$test${RED} compilation failed"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
    else
        ((VALID_PASSED = VALID_PASSED + 1))
        echo -e "${GREENBOLD}Test passed ($VALID_PASSED/$NB_VALID_TESTS): ${NOCOLOR}$test${GREEN} compilation of interactive test successfull"
    fi
done


VALID_PASSED_PERCENTAGE=`echo "$VALID_PASSED / $NB_VALID_TESTS * 100" | bc -l`

TEMP=`echo "$VALID_PASSED_PERCENTAGE > 0.5" | bc -l`

if ((TEMP));
then
    echo -e "\n${GREEN} Valid test passed: "
    printf %2.0f $VALID_PASSED_PERCENTAGE
    echo -e "%\n"
else
    echo -e "\n${RED} Valid test passed: "
    printf %2.0f $VALID_PASSED_PERCENTAGE
    echo -e "%\n"
fi

rm ./src/test/deca/codegen/*/*-temp.res
rm ./src/test/deca/codegen/*/*.ass