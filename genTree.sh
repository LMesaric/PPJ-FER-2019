#!/bin/bash

# read from stdin
src='cat'

# read from file
if [[ $# -eq 1 ]]; then
  src="cat $1"
fi

# assert cd is the location of the script
cd "${0%/*}"

run='java -cp target/classes'
base='src/main/resources'
node='node src/test/resources/friscjs/consoleapp/frisc-console.js'

# lexer definitions
cat $base/ppjC.lan | $run lab1.GLA && \
  # lexer parsing
  $src | ($run lab1.analizator.LA && \
  # syntax definitions
  cat $base/ppjC.san | $run lab2.GSA) | \
  # syntax parsing
  $run lab2.analizator.SA | \
  # generate FRISC code in a.frisc
  $run lab4.GeneratorKoda && \
  # execute the code
  $node src/main/java/lab4/a.frisc
