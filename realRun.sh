#!/bin/bash

gcc $1 -o gcc.out
./gcc.out
echo $?
