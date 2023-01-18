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

files=$(find ./src/test/deca/codegen/valid -maxdepth 1 -name "*.deca")

for test in $files
do
    ((NB_TESTS = NB_TESTS + 1))
    decac "$test"
    PERF_NO_OPTI=$(ima -s "${test%.deca}.ass")
    
    PERF_NO_OPTI=($PERF_NO_OPTI)
    NB_INST_NO_OPTI=${PERF_NO_OPTI[-1]}
    TPS_NO_OPTI=${PERF_NO_OPTI[-5]}

    decac -o "$test"
    PERF_OPTI=$(ima -s "${test%.deca}.ass")
    
    PERF_OPTI=($PERF_OPTI)
    NB_INST_OPTI=${PERF_OPTI[-1]}
    TPS_OPTI=${PERF_OPTI[-5]}

    if [ $NB_INST_OPTI -le $NB_INST_NO_OPTI -a $TPS_OPTI -le $TPS_NO_OPTI ];
    then
        COLOR=$GREEN
    else
        COLOR=$RED
    fi

    echo -e "${COLOR} Nb inst: $NB_INST_NO_OPTI -> $NB_INST_OPTI \t Tps: $TPS_NO_OPTI -> $TPS_OPTI \t ${test:24}"
    
done