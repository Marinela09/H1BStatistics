#!/bin/bash
#
# Use this shell script to compile (if necessary) your code and then execute it. Below is an example of what might be found in this file if your program was written in Python
#
javac ./src/H1BCounting.java
java -cp ./src/ H1BCounting < ./input/h1b_input.csv

