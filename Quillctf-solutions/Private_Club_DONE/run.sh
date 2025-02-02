#!/bin/bash

if ! command -v brownie &> /dev/null
then
    echo "Brownie not found. Installing..."
    # if falied use pip3
    pip install eth-brownie
else
    echo "Brownie already installed."
fi

echo "[*]Running test...."
brownie test

echo "[*]Running the attack script for debugging...."
brownie run scripts/attack.py

echo "[!]Check poc.gif for the solution poc.."