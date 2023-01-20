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
    if [ $? -ne 0 ]
    then
        exit 1
    fi
else
    option-b
fi

if [[ $1 == "--maven" ]];
then
    option-p --maven
    if [ $? -ne 0 ]
    then
        exit 1
    fi
else
    option-p
fi

if [[ $1 == "--maven" ]];
then
    option-v --maven
    if [ $? -ne 0 ]
    then
        exit 1
    fi
else
    option-v
fi

if [[ $1 == "--maven" ]];
then
    option-n --maven
    if [ $? -ne 0 ]
    then
        exit 1
    fi
else
    option-n
fi

if [[ $1 == "--maven" ]];
then
    option-r --maven
    if [ $? -ne 0 ]
    then
        exit 1
    fi
else
    option-r
fi

if [[ $1 == "--maven" ]];
then
    option-d --maven
    if [ $? -ne 0 ]
    then
        exit 1
    fi
else
    option-d
fi

if [[ $1 == "--maven" ]];
then
    option-parallel --maven
    if [ $? -ne 0 ]
    then
        exit 1
    fi
else
    option-parallel
fi

if [[ $1 == "--maven" ]];
then
    option-w --maven
    if [ $? -ne 0 ]
    then
        exit 1
    fi
else
    option-w
fi

if [[ $1 == "--maven" ]];
then
    other-cases --maven
    if [ $? -ne 0 ]
    then
        exit 1
    fi
else
    other-cases
fi