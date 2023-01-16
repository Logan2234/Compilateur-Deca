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

BRED='\033[1;31m'
RED='\033[0;31m'
BGREEN='\033[1;32m'
GREEN='\033[0;32m'
BWHITE='\033[1;37m'

# Stat var
NB_TESTS=0
NB_PASSED=0

echo -e "${BWHITE}\n============================= Lexer tests ============================="
echo -e "${BWHITE}compares the output with <test>.corr that contains a verified output\n"

files=$(find ./src/test/deca/syntax/valid/provided -name "*.deca")
files+=" "
files+=$(find ./src/test/deca/syntax/invalid/provided -name "*.deca")
files+=" "
files+=$(find ./src/test/deca/syntax/invalid/lexer -name "*.deca")

for test in $files
do
    ((NB_TESTS = NB_TESTS + 1))
    # save the output
    test_lex "$test" > "${test%.deca}"-lex.lis 2>&1

    # check the difference with the verified output
    diff=$(diff "${test%.deca}"-lex.lis "${test%.deca}"-lex.corr)
    test="${test:23}"

    if [ "$diff" = "" ]
    then
        ((NB_PASSED = NB_PASSED + 1))
        echo -e "${BGREEN}Test passed ($NB_PASSED/$NB_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${GREEN} returns the same result as in the correction"
    else
        echo -e "${BRED}Test failed ($NB_PASSED/$NB_TESTS): $NOCOLOR${test/.\/src\/test\/deca\//}${RED} does not return the same result as in the correction"
        if [[ $1 == "--maven" ]];
        then
            exit 1
        fi
    fi
done

VALID_PASSED_PERCENTAGE=`echo "$NB_PASSED / $NB_TESTS * 100" | bc -l`

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

rm ./src/test/deca/syntax/valid/provided/*.lis
rm ./src/test/deca/syntax/invalid/provided/*.lis
rm ./src/test/deca/syntax/invalid/lexer/*.lis