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
            Set<CodeBlock> alreadyReported = new HashSet<>();
            for (List<CodeBlock> group : duplicates.values()) {
                for (CodeBlock block : group) {
                    // Output every duplicate block in every group, even if it appears in multiple groups
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

        // --- JavaParser-based duplicate detection integration ---
        int javaParserDuplicateBlocks = 0;
        int javaParserTotalBlocks = 0;
        try {
            System.out.println("\n[JavaParser-based duplicate detection]\n");
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-cp", ".:lib/javaparser-core-3.25.4.jar:java", "JavaParserDuplicateDetection", "tests/EmployeeManager1.java"
            );
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            List<String> javaParserReportLines = new ArrayList<>();
            Set<String> uniqueBlocks = new HashSet<>();
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("  -> ")) {
                    javaParserReportLines.add(line + "\n");
                    uniqueBlocks.add(line.trim());
                }
            }
            proc.waitFor();
            javaParserDuplicateBlocks = uniqueBlocks.size();
            javaParserTotalBlocks = countJavaParserTotalBlocks("tests/EmployeeManager1.java");
            // Overwrite outputjava/duplicates_java.txt with only JavaParser-based results
            try (FileWriter fw = new FileWriter("outputjava/duplicates_java.txt", false)) {
                fw.write("[JavaParser-based duplicate detection]\n");
                Set<String> alreadyWritten = new HashSet<>();
                for (String l : javaParserReportLines) {
                    String trimmed = l.trim();
                    if (alreadyWritten.contains(trimmed)) continue;
                    alreadyWritten.add(trimmed);
                    fw.write(l);
                    // Parse block info:   -> Method name [start-end]
                    if (trimmed.startsWith("-> ")) {
                        String[] parts = trimmed.substring(3).split(" ");
                        if (parts.length >= 3) {
                            String type = parts[0];
                            String name = parts[1];
                            String lineRange = parts[2].replace("[","").replace("]","");
                            String[] nums = lineRange.split("-");
                            if (nums.length == 2) {
                                int start = Integer.parseInt(nums[0]);
                                int end = Integer.parseInt(nums[1]);
                                // Read code from tests/EmployeeManager1.java
                                List<String> fileLines = new ArrayList<>();
                                try (BufferedReader br = new BufferedReader(new FileReader("tests/EmployeeManager1.java"))) {
                                    String fileLine;
                                    while ((fileLine = br.readLine()) != null) fileLines.add(fileLine);
                                }
                                StringBuilder codeBlock = new StringBuilder();
                                for (int i = start - 1; i < end && i < fileLines.size(); i++) {
                                    codeBlock.append(fileLines.get(i)).append("\n");
                                }
                                fw.write(String.format("Duplicate block in java: %s (%d-%d)\n%s------\n", name, start, end, codeBlock.toString()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[JavaParser detection failed: " + e + "]");
        }

        // Calculate and report duplication scores
        int totalPythonBlocks = (int) allBlocks.stream().filter(b -> "python".equals(b.language)).count();
        int duplicatePythonBlocks = (int) allBlocks.stream().filter(b -> b.isDuplicate && "python".equals(b.language)).count();
        double pythonDuplicationScore = totalPythonBlocks > 0 ? (double) duplicatePythonBlocks / totalPythonBlocks : 0.0;
        double javaDuplicationScore = javaParserTotalBlocks > 0 ? (double) javaParserDuplicateBlocks / javaParserTotalBlocks : 0.0;
        String scoreReport = String.format(
            "Python Duplication Score: %.2f (%d duplicate blocks / %d total blocks)\n" +
            "Java Duplication Score: %.2f (%d duplicate blocks / %d total blocks)\n",
            pythonDuplicationScore, duplicatePythonBlocks, totalPythonBlocks,
            javaDuplicationScore, javaParserDuplicateBlocks, javaParserTotalBlocks
        );
        System.out.print(scoreReport);
        try (PrintWriter out = new PrintWriter("outputjava/duplication_score.txt")) {
            out.print(scoreReport);
        
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

    public static int countJavaParserTotalBlocks(String filename) {
        // Fallback: count all method and class headers in the file
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.matches(".*\\b(class|void|int|String|boolean|double|float|char)\\b.*\\(.*\\).*\\{.*") ||
                    line.matches(".*\\bclass\\b.*\\{.*")) {
                    count++;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return count;
    }
} 