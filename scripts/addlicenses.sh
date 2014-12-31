#!/bin/bash
echo "Prepend Apache Licenses to JSyn source."


file_year=0
files_missed=0
files_processed=0
license_dir=$HOME/Documents/workspace/JSyn/licenses
    
function prepend_license_file {
    filename=$1
    year=$2
    add_author=0
	if [ $year -eq 0 ]; then
        year=2009
    	add_author=1
    fi
    grep "Licensed under the Apache License" $filename > /dev/null
    if [ $? -eq 1 ]; then
		mv $filename temp2.txt
		cat $license_dir/license_part1.txt >temp1.txt
		echo " * Copyright $year Phil Burk, Mobileer Inc" >> temp1.txt
		cat $license_dir/license_part2.txt >> temp1.txt
	    if [ $add_author -eq 1 ]; then
		    cat $license_dir/license_part3.txt >> temp1.txt
		fi
		cat temp1.txt temp2.txt > $filename
		rm temp1.txt
		rm temp2.txt
    	(( files_processed += 1 ))
	fi
}

function process_file_year {
    filename=$1
    year=$2
    grep "(C) $year" $filename > /dev/null
    if [ $? -eq 0 ]; then
        # echo "prepend $year license file"
        prepend_license_file $filename $year
        file_year=$year
    fi
}

function process_java_file {
    filename=$1
    # echo "process $filename"
    file_year=0
    N=1997
    while [[ $N -lt 2015 && $file_year -eq 0 ]]; do
        process_file_year $filename $N
	    let N=N+1
    done
    if [ $file_year -eq 0 ]; then
    	(( files_missed += 1 ))
        echo "FILE $1 did not have a copyright."
        prepend_license_file $filename 0
    fi
}

function process_directory {
	for filename in *.java; do
		if [ -e $filename ]; then 
	    	process_java_file $filename
	    fi
	done
	# now scan subdirectories
	for filename in *; do
	    if [ -d "$filename" ]; then
	          echo "$filename is a directory"
	          cd $filename
	          pwd
	          process_directory
	          cd ../
	    fi
	done
}

process_directory
# prepend_license_file src/com/jsyn/JSyn.java 2007
echo "$files_missed files missed"
echo "$files_processed files processed"
