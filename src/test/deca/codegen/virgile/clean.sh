#! /bin/sh


temp_files=$(find $wd -name "*.out")
for to_rm in $temp_files
do
    rm $to_rm
done

temp_files=$(find $wd -name "*.log")
for to_rm in $temp_files
do
    rm $to_rm
done

temp_files=$(find $wd -name "*.ass")
for to_rm in $temp_files
do
    rm $to_rm
done
