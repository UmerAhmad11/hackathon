// JavaBlocks.java
// Java version of java_blocks.py (rewritten to match logic exactly)
// Extracts code blocks from Java files for duplicate detection.
// Place this file in the java/ directory.

import java.io.*;
import java.util.*;
import java.util.regex.*;

// Represents a node in the Java AST-like structure
class JavaNode {
    String type;
    String name;
    int startLine;
    int endLine;
    String body;

    public JavaNode(String type, String name, int startLine, int endLine, String body) {
        this.type = type;
        this.name = name;
        this.startLine = startLine;
        this.endLine = endLine;
        this.body = body;
    }
}

public class JavaBlocks {
    // Extracts a block of code delimited by braces
    private static BlockExtractionResult extractBlock(List<String> lines, int start) {
        List<String> body = new ArrayList<>();
        body.add(lines.get(start));
        int braceCount = countChar(lines.get(start), '{') - countChar(lines.get(start), '}');
        int i = start + 1;
        while (i < lines.size() && braceCount > 0) {
            String line = lines.get(i);
            body.add(line);
            braceCount += countChar(line, '{') - countChar(line, '}');
            i++;
        }
        return new BlockExtractionResult(body, start, i - 1);
    }

    // Helper to count occurrences of a character
    private static int countChar(String s, char c) {
        int count = 0;
        for (char ch : s.toCharArray()) if (ch == c) count++;
        return count;
    }

    // Parses lines to extract Java nodes (classes, methods, control blocks)
    private static ParseResult parseLines(List<String> lines, int absStartIdx) {
        List<JavaNode> nodes = new ArrayList<>();
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i).trim();
            // Match class
            Matcher m = Pattern.compile("(public|private|protected)?\\s*class\\s+(\\w+)").matcher(line);
            if (m.find()) {
                String name = m.group(2);
                BlockExtractionResult blockRes = extractBlock(lines, i);
                JavaNode node = new JavaNode("class", name, absStartIdx + blockRes.start + 1, absStartIdx + blockRes.end + 1, String.join("", blockRes.body));
                nodes.add(node);
                if (blockRes.body.size() > 1) {
                    int subBodyStart = i + 1;
                    int subBodyEnd = blockRes.end + 1;
                    ParseResult subnodes = parseLines(lines.subList(subBodyStart, subBodyEnd), absStartIdx + subBodyStart);
                    nodes.addAll(subnodes.nodes);
                }
                i = blockRes.end + 1;
                continue;
            }
            // Match method
            m = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*\\w+\\s+(\\w+)\\s*\\(.*\\)\\s*\\{").matcher(line);
            if (m.find()) {
                String name = m.group(3);
                BlockExtractionResult blockRes = extractBlock(lines, i);
                JavaNode node = new JavaNode("method", name, absStartIdx + blockRes.start + 1, absStartIdx + blockRes.end + 1, String.join("", blockRes.body));
                nodes.add(node);
                i = blockRes.end + 1;
                continue;
            }
            // Match control blocks
            boolean matched = false;
            for (String keyword : Arrays.asList("if", "else if", "else", "for", "while", "do")) {
                String pattern = "^" + keyword + "\\b.*\\{";
                if (Pattern.compile(pattern).matcher(line).find()) {
                    String name = keyword.toUpperCase() + "_BLOCK_" + (absStartIdx + i + 1);
                    BlockExtractionResult blockRes = extractBlock(lines, i);
                    JavaNode node = new JavaNode("control", name, absStartIdx + blockRes.start + 1, absStartIdx + blockRes.end + 1, String.join("", blockRes.body));
                    nodes.add(node);
                    i = blockRes.end + 1;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                i++;
            }
        }
        return new ParseResult(nodes, i);
    }

    // Helper class for block extraction result
    private static class BlockExtractionResult {
        List<String> body;
        int start;
        int end;
        BlockExtractionResult(List<String> body, int start, int end) {
            this.body = body;
            this.start = start;
            this.end = end;
        }
    }
    // Helper class for parse result
    private static class ParseResult {
        List<JavaNode> nodes;
        int i;
        ParseResult(List<JavaNode> nodes, int i) {
            this.nodes = nodes;
            this.i = i;
        }
    }

    // Extracts all code blocks from a Java file
    public static List<CodeBlock> extractJavaBlocks(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        ParseResult parseRes = parseLines(lines, 0);
        List<CodeBlock> blocks = new ArrayList<>();
        for (JavaNode node : parseRes.nodes) {
            String[] blockLines = node.body.split("\n");
            String bodyOnly;
            if (node.type.equals("method") && blockLines.length > 0) {
                // Join all lines to a single string
                String fullBlock = String.join("\n", blockLines);
                int openIdx = fullBlock.indexOf('{');
                int closeIdx = fullBlock.lastIndexOf('}');
                if (openIdx != -1 && closeIdx != -1 && closeIdx > openIdx) {
                    String inner = fullBlock.substring(openIdx + 1, closeIdx);
                    // Split into lines, trim each line, and join
                    String[] innerLines = inner.split("\n");
                    List<String> trimmed = new ArrayList<>();
                    for (String l : innerLines) {
                        String t = l.trim();
                        if (!t.isEmpty()) trimmed.add(t);
                    }
                    bodyOnly = String.join(" ", trimmed);
                } else {
                    bodyOnly = "";
                }
            } else {
                // For other blocks, use the whole block
                List<String> trimmed = new ArrayList<>();
                for (String l : blockLines) {
                    String t = l.trim();
                    if (!t.isEmpty()) trimmed.add(t);
                }
                bodyOnly = String.join(" ", trimmed);
            }
            CodeBlock cb = new CodeBlock(node.name, node.startLine, node.endLine, node.body, "java");
            try {
                java.lang.reflect.Field f = cb.getClass().getField("body_only");
                f.set(cb, bodyOnly);
            } catch (Exception e) {
                // Ignore if field doesn't exist
            }
            blocks.add(cb);
        }
        return blocks;
    }

    // DEBUG: Print all detected blocks
    public static void debugPrintBlocks(List<CodeBlock> blocks) {
        for (CodeBlock block : blocks) {
            System.out.printf("[DEBUG] Block: %s | Type: %s | Lines: %d-%d\n", block.name, block.getClass().getSimpleName(), block.startLine, block.endLine);
        }
    }
    // DEBUG: Print normalized code for each block
    public static void debugPrintNormalized(List<CodeBlock> blocks) {
        for (CodeBlock block : blocks) {
            String codeToNormalize;
            try {
                codeToNormalize = (String) block.getClass().getField("body_only").get(block);
                if (codeToNormalize == null) codeToNormalize = block.code;
            } catch (Exception e) {
                codeToNormalize = block.code;
            }
            String normalized = DuplicateDetection.normalizeCode(codeToNormalize);
            System.out.printf("[DEBUG] Block: %s | Normalized: %s\n", block.name, normalized);
        }
    }
} 