#! /bin/sh

# full test for the compiler, made by and for Virgile HENRY.
wd=$(dirname "$0")
files=$(find $wd -name "*.deca" | sort -n)

total=0
passed=0

RED='\033[0;31m'
GREEN='\033[1;32m'
WHITE='\033[1;37m'

# start by cleanup
./$wd/clean.sh

for test in $files
do
    total=$(($total+1))
    # generate the assembly
    decac $test > ${test%.deca}.log 2>&1
    # run it through the virtual machine, output in temp file
    ima ${test%.deca}.ass > ${test%.deca}.out
    # compare result and out
    name=$(basename $test)
    if cmp -s ${test%.deca}.out ${test%.deca}.result;
    then
        passed=$(($passed+1))
        echo "$GREEN Test ($name) passed ! ($passed/$total)"
    else
        echo "$RED Test ($name) failed ! ($passed/$total)"
    fi
done

failed=$(($total - $passed))
echo "$WHITE \n==============\n"
echo "Done testing ($total done, $passed passed, $failed failed"