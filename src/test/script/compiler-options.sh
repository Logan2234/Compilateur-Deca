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

if [[ $1 == "--maven" ]];
then
    option-p --maven
else
    option-p
fi

if [[ $1 == "--maven" ]];
then
    option-v --maven
else
    option-v
fi

# TODO: Supprimer les fichiers inutiles .lis etc