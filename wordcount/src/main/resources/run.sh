#!/usr/bin/env bash
cd ../../..
# mvn clean compile package
hadoop jar target/wordcount-1.0-SNAPSHOT.jar WordCount input output
