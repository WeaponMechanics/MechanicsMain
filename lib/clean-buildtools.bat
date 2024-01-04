#!/bin/bash

echo "Deleting directory BuildTools work to free space... This may take a while"

# Remove the BuildTools directory and its contents
rm -rf BuildTools

echo "Deleted BuildTools"

# Pause and wait for user input before exiting
read -p "Press any key to continue... " -n1 -s
echo
