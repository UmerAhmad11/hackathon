// DuplicateDetection.java
// Java version of duplicate_detection.py
// Detects duplicate code blocks between Java and Python files.
// Place this file in the java/ directory.

import java.util.*;

public class DuplicateDetection {
    // Normalize code by removing comments, whitespace, and lowercasing
    public static String normalizeCode(String code) {
        // Remove single-line comments (// and #)
        code = code.replaceAll("//.*", "");
        code = code.replaceAll("#.*", "");
        // Remove multi-line comments (/* ... */)
        code = code.replaceAll("(?s)/\\*.*?\\*/", "");
        // Remove all whitespace (spaces, tabs, newlines)
        code = code.replaceAll("\\s+", "");
        // Lowercase
        code = code.toLowerCase();
        return code;
    }

    // Mark duplicates among code blocks and group them
    public static Map<String, List<CodeBlock>> markDuplicates(List<CodeBlock> blocks) {
        Map<String, List<CodeBlock>> duplicatesGroups = new HashMap<>();
        List<String> normalizedBodies = new ArrayList<>();
        List<CodeBlock> allBlocks = new ArrayList<>(blocks);

        // Build normalized body_only list
        for (CodeBlock block : allBlocks) {
            String codeToNormalize;
            try {
                codeToNormalize = (String) block.getClass().getField("body_only").get(block);
                if (codeToNormalize == null || codeToNormalize.trim().isEmpty()) codeToNormalize = block.code;
            } catch (Exception e) {
                codeToNormalize = block.code;
            }
            String normalized = normalizeCode(codeToNormalize);
            normalizedBodies.add(normalized);
        }

        // Brute-force compare all pairs
        for (int i = 0; i < allBlocks.size(); i++) {
            for (int j = i + 1; j < allBlocks.size(); j++) {
                if (normalizedBodies.get(i).equals(normalizedBodies.get(j))) {
                    allBlocks.get(i).isDuplicate = true;
                    allBlocks.get(j).isDuplicate = true;
                    // Add both to the group for this normalized body
                    String key = normalizedBodies.get(i);
                    duplicatesGroups.computeIfAbsent(key, k -> new ArrayList<>());
                    if (!duplicatesGroups.get(key).contains(allBlocks.get(i)))
                        duplicatesGroups.get(key).add(allBlocks.get(i));
                    if (!duplicatesGroups.get(key).contains(allBlocks.get(j)))
                        duplicatesGroups.get(key).add(allBlocks.get(j));
                }
            }
        }
        return duplicatesGroups;
    }
} 