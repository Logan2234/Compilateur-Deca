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
NB_INVALID_TESTS=0
VALID_PASSED=0
INVALID_PASSED=0

# On se place dans le répertoire du projet (quel que soit le
# répertoire d'où est lancé le script) :
cd "$(dirname "$0")"/../../../ || exit 1

PATH=./src/test/script/launchers:"$PATH"

echo -e "${BWHITE} \n============================= Context valid tests =============================\n"

files=$(find ./src/test/deca/context/valid -maxdepth 1 -name "*.deca")

for test in $files
do
    ((NB_VALID_TESTS = NB_VALID_TESTS + 1))
    test_context "$test" > /dev/null 2>&1
    
    if [ $? -ne 0 ]
    then
        echo -e "${REDBOLD}Test failed ($VALID_PASSED/$NB_VALID_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${RED} returns an error"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
    else
        ((VALID_PASSED = VALID_PASSED + 1))
        echo -e "${GREENBOLD}Test passed ($VALID_PASSED/$NB_VALID_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${GREEN} returns no error"
    fi
done

echo -e "${BWHITE} \n============================= Context invalid tests =============================\n"

files=$(find ./src/test/deca/context/invalid -maxdepth 1 -name "*.deca")

for test in $files
do
    ((NB_INVALID_TESTS = NB_INVALID_TESTS + 1))
    if test_context "$test" 2>&1 | grep -q "$test:*:*"
    then
        ((INVALID_PASSED = INVALID_PASSED + 1))
        echo -e "${GREENBOLD}Test passed ($INVALID_PASSED/$NB_INVALID_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${GREEN} returns a correct error"
    else
        echo -e "${REDBOLD}Test failed ($INVALID_PASSED/$NB_INVALID_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${RED} returns no error"
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