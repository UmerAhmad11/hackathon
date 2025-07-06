// Main.java
// Java version of main.py
// Orchestrates duplicate detection between Java and Python codebases.
// Place this file in the java/ directory.

import java.io.*;
import java.util.*;

public class Main {
    // Utility to run scan() on all blocks (no threading for simplicity)
    public static void runScan(List<CodeBlock> blocks) {
        for (CodeBlock block : blocks) {
            block.scan();
        }
    }

    // Extract blocks based on file extension
    public static List<CodeBlock> extractBlocks(String filename) throws IOException {
        if (filename.endsWith(".py")) {
            return PythonBlocks.extractPythonBlocks(filename);
        } else if (filename.endsWith(".java")) {
            return JavaBlocks.extractJavaBlocks(filename);
        } else {
            System.out.println("Unsupported file type: " + filename);
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) throws IOException {
        // Use the new tests directory for input files
        String[] filenames = {"tests/EmployeeManager1.java", "tests/python_test.py"};
        List<CodeBlock> allBlocks = new ArrayList<>();

        for (String file : filenames) {
            File f = new File(file);
            if (f.exists()) {
                allBlocks.addAll(extractBlocks(file));
            } else {
                System.out.println("File not found: " + file);
            }
        }

        Map<String, List<CodeBlock>> duplicates = DuplicateDetection.markDuplicates(allBlocks);

        // Prepare report data
        List<String> pythonReportLines = new ArrayList<>();
        List<String> javaReportLines = new ArrayList<>();

        if (!duplicates.isEmpty()) {
            System.out.println("Duplicate blocks found:\n");
            for (List<CodeBlock> group : duplicates.values()) {
                for (CodeBlock block : group) {
                    if (block.isDuplicate) {
                        String entry = String.format(
                            "Duplicate block in %s: %s (%d-%d)\n%s\n------\n",
                            block.language, block.name, block.startLine, block.endLine, block.code.strip()
                        );
                        System.out.print(entry);
                        if (block.language.equals("python")) {
                            pythonReportLines.add(entry);
                        } else if (block.language.equals("java")) {
                            javaReportLines.add(entry);
                        }
                    }
                }
            }
        } else {
            System.out.println("No duplicate blocks found.");
            pythonReportLines.add("No duplicate blocks found.\n");
            javaReportLines.add("No duplicate blocks found.\n");
        }

        // Write to output files
        try (PrintWriter out = new PrintWriter("outputjava/duplicates_python.txt")) {
            for (String line : pythonReportLines) out.print(line);
        }
        try (PrintWriter out = new PrintWriter("outputjava/duplicates_java.txt")) {
            for (String line : javaReportLines) out.print(line);
        }

        // Run scan (no threads)
        runScan(allBlocks);

        // Calculate and report duplication scores
        int totalPythonBlocks = (int) allBlocks.stream().filter(b -> "python".equals(b.language)).count();
        int totalJavaBlocks = (int) allBlocks.stream().filter(b -> "java".equals(b.language)).count();
        int duplicatePythonBlocks = (int) allBlocks.stream().filter(b -> b.isDuplicate && "python".equals(b.language)).count();
        int duplicateJavaBlocks = (int) allBlocks.stream().filter(b -> b.isDuplicate && "java".equals(b.language)).count();

        double pythonDuplicationScore = totalPythonBlocks > 0 ? (double) duplicatePythonBlocks / totalPythonBlocks : 0.0;
        double javaDuplicationScore = totalJavaBlocks > 0 ? (double) duplicateJavaBlocks / totalJavaBlocks : 0.0;

        String scoreReport = String.format(
            "Python Duplication Score: %.2f (%d duplicate blocks / %d total blocks)\n" +
            "Java Duplication Score: %.2f (%d duplicate blocks / %d total blocks)\n",
            pythonDuplicationScore, duplicatePythonBlocks, totalPythonBlocks,
            javaDuplicationScore, duplicateJavaBlocks, totalJavaBlocks
        );
        System.out.print(scoreReport);
        try (PrintWriter out = new PrintWriter("outputjava/duplication_score.txt")) {
            out.print(scoreReport);
            out.print(totalPythonBlocks);
        }

        // Write all detected Python blocks to output/all_python_blocks.txt
        /* 
        try (PrintWriter out = new PrintWriter("output/all_python_blocks.txt")) {
            for (CodeBlock block : allBlocks) {
                if ("python".equals(block.language)) {
                    String entry = String.format(
                        "Python block: %s (%d-%d)\n%s\n------\n",
                        block.name, block.startLine, block.endLine, block.code.strip()
                    );
                    out.print(entry);
                }
            }
        }*/
    }
} 