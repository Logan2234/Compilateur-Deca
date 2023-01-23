#!/bin/bash

# Colors:
RED='\033[0;31m'
REDBOLD='\033[0;31;1m'
GREENBOLD='\033[0;32;1m'
GREEN='\033[0;32m'
NOCOLOR='\033[0m'
BWHITE='\033[1;37m'

# On se place dans le répertoire du projet (quel que soit le
# répertoire d'où est lancé le script) :
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

echo -e "${BWHITE} \n============================= Optim tests =============================\n"

files=$(find ./src/test/deca/optim/ -name "*.deca")

for test in $files
do
    if decac "$test" 2>&1 | grep -q "${test}\|Exception in thread"
    then
        echo -e "${REDBOLD}Test failed: $NOCOLOR${test/.\/src\/test\/deca\//}${RED} compilation failed"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
    else
        mv "${test%.deca}".ass "${test%.deca}"-without-opti.ass
        if decac -o "$test" 2>&1 | grep -q "${test}\|Exception in thread"
        then
            echo -e "${REDBOLD}Test failed: $NOCOLOR${test/.\/src\/test\/deca\//}${RED} compilation failed with -o option"
            if [[ $1 == "--maven" ]];
            then
                exit 1
            fi
        else
            ima "${test%.deca}"-without-opti.ass > /dev/null
            if [ $? -ne 0 ]
            then
                echo -e "${REDBOLD}Test failed: $NOCOLOR${test/.\/src\/test\/deca\//}${RED} execution of the optimized file failed"
                if [[ $1 == "--maven" ]];
                then
                    exit 1
                fi
            else
                i=$(wc --lines "${test%.deca}"-without-opti.ass | cut -c1-3)
                j=$(wc --lines "${test%.deca}".ass | cut -c1-3)
                if [ i \> j ]
                then
                    echo -e "${GREENBOLD}Test passed: $NOCOLOR${test/.\/src\/test\/deca\//}$GREEN optimized file has less lines than original file"
                else
                    echo -e "${REDBOLD}Test failed: $NOCOLOR${test/.\/src\/test\/deca\//}${RED} optimized file does not have less lines than original file"
                    # if [[ $1 == "--maven" ]];
                    # then
                    #     exit 1
                    # fi
                fi
            fi
        fi
    fi
done

rm ./src/test/deca/optim/*/*.ass