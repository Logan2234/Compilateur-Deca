#!/bin/bash

# Colors:
RED='\033[0;31m'
REDBOLD='\033[0;31;1m'
GREENBOLD='\033[0;32;1m'
GREEN='\033[0;32m'
NOCOLOR='\033[0m'
BWHITE='\033[1;37m'

# Stat var
NB_TESTS=0
NB_PASSED=0

# On se place dans le répertoire du projet (quel que soit le
# répertoire d'où est lancé le script) :
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"
PATH=./src/test/script/options-scripts:"$PATH"

if [[ $1 == "--maven" ]];
then
    option-b --maven
else
    option-b
fi


# files=$(find ./src/test/deca/codegen/valid -maxdepth 1 -name "*.deca")
# files+=$(find ./src/test/deca/context/valid -maxdepth 1 -name "*.deca")

# for test in $files
# do
    
# done

# VALID_PASSED_PERCENTAGE=`echo "$NB_PASSED / $NB_TESTS * 100" | bc -l`

# TEMP=`echo "$VALID_PASSED_PERCENTAGE > 0.5" | bc -l`

# if ((TEMP));
# then
#     echo -e "\n${GREEN} Valid test passed: "
#     printf %2.0f $VALID_PASSED_PERCENTAGE
#     echo "%"
# else
#     echo -e "\n${RED} Valid test passed: "
#     printf %2.0f $VALID_PASSED_PERCENTAGE
#     echo "%"
# fi

# TEMP=`echo "$INVALID_PASSED_PERCENTAGE > 0.5" | bc -l`
# if ((TEMP));
# then
#     echo -e "\n${GREEN} Invalid test passed: "
#     printf %2.0f $INVALID_PASSED_PERCENTAGE
#     echo -e "%\n"
# else
#     echo -e "\n${RED} Invalid test passed: "
#     printf %2.0f $INVALID_PASSED_PERCENTAGE
#     echo -e "%\n"
# fi