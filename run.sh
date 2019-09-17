#!/bin/bash

working_folder="$(cd -P -- "$(dirname -- "$0")" && pwd -P)"
srcdir="$working_folder"/src
classdir="$working_folder"/bin

# compile
javac -classpath "$classdir" "$srcdir"/*.java -d "$classdir"

# run the experiments
mkdir -p $working_folder/results_coin
for file in $working_folder/input/coin*; do
  filename=$(echo $file | rev | cut -d / -f 1 | rev)
  if [[ $filename == *"labels" ]];then
    continue
  fi
  labelfile="$file"_labels
  echo $filename
  fileResult=$working_folder/results_coin/"$filename".log
  java -Xss20480k  -classpath "$classdir" PerformanceDistanceOne $file $labelfile | tee $fileResult
done

