#!/bin/bash

# On se place dans le répertoire du projet (quel que soit le
# répertoire d'où est lancé le script) :
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

# valid tests
files=$(find ./src/test/deca/context/valid -name "*.deca")

for test in $files
do
    test_context "$test" > "${test%.deca}".lis 2>&1 # TODO: Bien sûr ce script est à améliorer
done

# invalid tests
files=$(find ./src/test/deca/context/invalid -name "*.deca")

for test in $files
do
    test_context "$test" > "${test%.deca}".lis 2>&1
done