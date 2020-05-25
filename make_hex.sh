#!/bin/bash

DIR_BIN=rafi-prebuilt-binary/riscv-tests/isa
DIR_HEX_DCACHE=hex/dcache
DIR_HEX_ICACHE=hex/icache

mkdir -p ${DIR_HEX_DCACHE}
mkdir -p ${DIR_HEX_ICACHE}

for bin_path in `ls $DIR_BIN/rv64ui-p-*.bin`; do
    bin_filename=${bin_path##*/}
    hex_filename=${bin_filename%.*}.hex
    od -A n -v -t x1 -w8 $bin_path > $DIR_HEX_DCACHE/$hex_filename
    od -A n -v -t x4 -w4 $bin_path > $DIR_HEX_ICACHE/$hex_filename

    hex_0_filename=${bin_filename%.*}_0.hex
    hex_1_filename=${bin_filename%.*}_1.hex
    hex_2_filename=${bin_filename%.*}_2.hex
    hex_3_filename=${bin_filename%.*}_3.hex
    hex_4_filename=${bin_filename%.*}_4.hex
    hex_5_filename=${bin_filename%.*}_5.hex
    hex_6_filename=${bin_filename%.*}_6.hex
    hex_7_filename=${bin_filename%.*}_7.hex

    awk '{print $1}' $DIR_HEX_DCACHE/$hex_filename > $DIR_HEX_DCACHE/$hex_0_filename
    awk '{print $2}' $DIR_HEX_DCACHE/$hex_filename > $DIR_HEX_DCACHE/$hex_1_filename
    awk '{print $3}' $DIR_HEX_DCACHE/$hex_filename > $DIR_HEX_DCACHE/$hex_2_filename
    awk '{print $4}' $DIR_HEX_DCACHE/$hex_filename > $DIR_HEX_DCACHE/$hex_3_filename
    awk '{print $5}' $DIR_HEX_DCACHE/$hex_filename > $DIR_HEX_DCACHE/$hex_4_filename
    awk '{print $6}' $DIR_HEX_DCACHE/$hex_filename > $DIR_HEX_DCACHE/$hex_5_filename
    awk '{print $7}' $DIR_HEX_DCACHE/$hex_filename > $DIR_HEX_DCACHE/$hex_6_filename
    awk '{print $8}' $DIR_HEX_DCACHE/$hex_filename > $DIR_HEX_DCACHE/$hex_7_filename
done
