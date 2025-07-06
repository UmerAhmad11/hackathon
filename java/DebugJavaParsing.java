// DebugJavaParsing.java
// Java version of debug_java_parsing.py
// Debug script to test Java parsing and block extraction.
// Place this file in the java/ directory.

import java.io.*;
import java.util.*;

public class DebugJavaParsing {
    // Debug the Java parsing to see what blocks are extracted
    public static void debugJavaParsing() throws IOException {
        String filename = "tests/EmployeeManager1.java";
        List<CodeBlock> blocks = JavaBlocks.extractJavaBlocks(filename);

        System.out.println("=== Java Blocks Extracted ===");
        System.out.println("Total blocks found: " + blocks.size());
        System.out.println();

        for (int i = 0; i < blocks.size(); i++) {
            CodeBlock block = blocks.get(i);
            System.out.printf("Block %d: %s (lines %d-%d)\n", i + 1, block.name, block.startLine, block.endLine);
            // Type is not a field in CodeBlock, so we print the name as a proxy
            System.out.printf("Type: %s\n", block.name);
            System.out.println("Code preview:");
            String[] lines = block.code.split("\n");
            for (int j = 0; j < Math.min(3, lines.length); j++) {
                System.out.printf("  %d: %s\n", j + 1, lines[j]);
            }
            if (lines.length > 3) {
                System.out.printf("  ... (%d more lines)\n", lines.length - 3);
            }
            System.out.println("--------------------------------------------------");
        }
    }

    public static void main(String[] args) throws IOException {
        debugJavaParsing();
    }
} 