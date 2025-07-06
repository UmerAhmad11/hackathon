// PythonBlocks.java
// Author: Replica of python_blocks.py in Java
// Description: Extracts code blocks (functions, classes, loops, conditionals) from Python files for analysis.

import java.io.*;
import java.util.*;
import java.util.regex.*;

// Represents a code block (function, class, loop, or conditional)
class CodeBlock {
    String name;         // Name of the block (function/class name, or type for loops/conditionals)
    int startLine;       // Starting line number (1-indexed)
    int endLine;         // Ending line number (1-indexed, exclusive)
    String code;         // The code content of the block
    String language;     // Language of the code block ("python")
    boolean isDuplicate = false; // Flag for duplicate detection (not used here)

    // Constructor to initialize a code block
    public CodeBlock(String name, int startLine, int endLine, String code, String language) {
        this.name = name;
        this.startLine = startLine;
        this.endLine = endLine;
        this.code = code;
        this.language = language;
    }

    // Print the block if it's marked as duplicate (utility method)
    public void scan() {
        if (isDuplicate) {
            System.out.printf("Duplicate block in %s: %s (%d-%d)\n", language, name, startLine, endLine);
            System.out.println(code.strip());
            System.out.println("------");
        }
    }
}

public class PythonBlocks {
    // Regular expressions to match Python block headers
    private static final Pattern FUNC_PATTERN = Pattern.compile("^\s*def\\s+(\\w+)\\s*\\("); // function
    private static final Pattern CLASS_PATTERN = Pattern.compile("^\s*class\\s+(\\w+)\\s*\\(?"); // class
    private static final Pattern FOR_PATTERN = Pattern.compile("^\s*for\\s+.*:"); // for loop
    private static final Pattern WHILE_PATTERN = Pattern.compile("^\s*while\\s+.*:"); // while loop
    private static final Pattern IF_PATTERN = Pattern.compile("^\s*if\\s+.*:"); // if statement
    private static final Pattern ELIF_PATTERN = Pattern.compile("^\s*elif\\s+.*:"); // elif statement
    private static final Pattern ELSE_PATTERN = Pattern.compile("^\s*else\\s*:"); // else statement

    // Extracts all code blocks from a Python file
    public static List<CodeBlock> extractPythonBlocks(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        // Read all lines from the file
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        List<CodeBlock> blocks = new ArrayList<>();
        // Iterate through each line to find block headers
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher mFunc = FUNC_PATTERN.matcher(line);
            Matcher mClass = CLASS_PATTERN.matcher(line);
            Matcher mFor = FOR_PATTERN.matcher(line);
            Matcher mWhile = WHILE_PATTERN.matcher(line);
            Matcher mIf = IF_PATTERN.matcher(line);
            Matcher mElif = ELIF_PATTERN.matcher(line);
            Matcher mElse = ELSE_PATTERN.matcher(line);
            String name = null;
            // Determine the type of block and extract its name/type
            if (mFunc.find()) {
                name = mFunc.group(1); // function name
            } else if (mClass.find()) {
                name = mClass.group(1); // class name
            } else if (mFor.find()) {
                name = "For";
            } else if (mWhile.find()) {
                name = "While";
            } else if (mIf.find()) {
                name = "If";
            } else if (mElif.find()) {
                name = "Elif";
            } else if (mElse.find()) {
                name = "Else";
            }
            // If a block header is found, extract the block
            if (name != null) {
                int start = i;
                int end = findBlockEnd(start, lines); // Find where the block ends
                StringBuilder blockCode = new StringBuilder();
                for (int j = start; j < end; j++) {
                    blockCode.append(lines.get(j)).append("\n");
                }
                // Add the block to the list
                blocks.add(new CodeBlock(name, start + 1, end, blockCode.toString(), "python"));
                i = end - 1; // Skip to the end of the block
            }
        }
        return blocks;
    }

    // Finds the end of a block by comparing indentation
    private static int findBlockEnd(int start, List<String> lines) {
        int baseIndent = getIndent(lines.get(start)); // Indentation of the block header
        for (int i = start + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue; // Skip empty lines
            int currIndent = getIndent(line);
            if (currIndent <= baseIndent) return i; // Block ends when indentation decreases
        }
        return lines.size(); // End of file
    }

    // Returns the indentation (number of leading whitespace characters) of a line
    private static int getIndent(String line) {
        int count = 0;
        while (count < line.length() && Character.isWhitespace(line.charAt(count))) count++;
        return count;
    }

    // Example usage: java PythonBlocks <python_file.py>
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java PythonBlocks <python_file.py>");
            return;
        }
        // Extract and print all code blocks from the given Python file
        List<CodeBlock> blocks = extractPythonBlocks(args[0]);
        for (CodeBlock block : blocks) {
            System.out.printf("Block: %s (%d-%d)\n", block.name, block.startLine, block.endLine);
            System.out.println(block.code);
            System.out.println("------");
        }
    }
} 