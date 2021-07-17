#!/bin/sh
set -ex
wget https://github.com/samtools/bcftools/releases/download/1.13/bcftools-1.13.tar.bz2
tar -xjvf bcftools-1.13.tar.bz2
cd bcftools-1.13 && make && sudo make prefix=/usr install
