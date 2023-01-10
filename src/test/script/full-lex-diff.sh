#!/bin/bash

# This script executes test_lex on all <example-name>.deca files in 
#   src/test/syntax/valid/provided/
#   src/test/syntax/invalid/provided/
#   src/test/syntax/invalid/lexer/
# The outputs are saved in files named <example-name>-lex.lis to differentiate them
# from the output files of full-synt.sh which is executed on the same files.

# Then the script compares the output files with the file <test>.corr containing the
# the output that we should obtain and that has been previously verified

# A file named *_lex.deca denotes an example that is syntaxically invalid but
# lexically correct.

# Change the current working directory to be in the project's directory
# wherever the script is executed from
cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

RED='\033[0;31m'
GREEN='\033[0;32m'
BWHITE='\033[1;37m'

echo -e "${BWHITE}#################### diff tests ####################"
echo -e "${BWHITE}compares the output with <test>.corr that contains a verified output
"
files=$(find ./src/test/deca/syntax/valid/provided -name "*.deca")
files+=" "
files+=$(find ./src/test/deca/syntax/invalid/provided -name "*.deca")
files+=" "
files+=$(find ./src/test/deca/syntax/invalid/lexer -name "*.deca")

for test in $files
do
    # save the output
    test_lex "$test" > "${test%.deca}"-lex.lis 2>&1

    # check the difference with the verified output
    diff=$(diff "${test%.deca}"-lex.lis "${test%.deca}"-lex.corr)
    test="${test:23}"

    if [ "$diff" = "" ]
    then
        echo -e "${GREEN}Pass: $test"
    else
        echo -e "${RED}Fail: $test"
    fi

done
