#!/bin/bash
set -e

JAR_VERSION=3.25.4
JAR_NAME=javaparser-core-$JAR_VERSION.jar
JAR_URL="https://repo1.maven.org/maven2/com/github/javaparser/javaparser-core/$JAR_VERSION/$JAR_NAME"
LIB_DIR="lib"
JAVA_DIR="java"
TESTS_DIR="tests"

# Create lib directory if it doesn't exist
mkdir -p "$LIB_DIR"

# Download JavaParser jar if not present
if [ ! -f "$LIB_DIR/$JAR_NAME" ]; then
    echo "Downloading JavaParser $JAR_VERSION..."
    curl -L -o "$LIB_DIR/$JAR_NAME" "$JAR_URL"
fi

# Compile the JavaParserDuplicateDetection.java file
javac -cp ".:$LIB_DIR/$JAR_NAME" "$JAVA_DIR/JavaParserDuplicateDetection.java"

# Run the duplicate detection on all .java files in tests/
echo "\nRunning JavaParser-based duplicate detection on all Java files in $TESTS_DIR..."
java -cp ".:$LIB_DIR/$JAR_NAME:$JAVA_DIR" JavaParserDuplicateDetection $TESTS_DIR/*.java 