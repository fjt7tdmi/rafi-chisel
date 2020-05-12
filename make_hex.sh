#!/bin/bash

DIR_BIN=rafi-prebuilt-binary/riscv-tests/isa
DIR_HEX=hex

mkdir -p ${DIR_HEX}

for bin_path in `ls $DIR_BIN/rv64ui-p-*.bin`; do
    bin_filename=${bin_path##*/}
    hex_filename=${bin_filename%.*}.hex
    od -A n -v -t x4 -w4 $bin_path > $DIR_HEX/$hex_filename
done
