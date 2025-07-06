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
        // Remove all whitespace
        code = code.replaceAll("\\s+", "");
        // Lowercase
        code = code.toLowerCase();
        return code;
    }

    // Mark duplicates among code blocks and group them
    public static Map<String, List<CodeBlock>> markDuplicates(List<CodeBlock> blocks) {
        Map<String, CodeBlock> seen = new HashMap<>();
        Map<String, List<CodeBlock>> duplicatesGroups = new HashMap<>();

        for (CodeBlock block : blocks) {
            // Use body_only if available, else code
            String codeToNormalize;
            try {
                codeToNormalize = (String) block.getClass().getField("body_only").get(block);
                if (codeToNormalize == null) codeToNormalize = block.code;
            } catch (Exception e) {
                codeToNormalize = block.code;
            }
            String normalized = normalizeCode(codeToNormalize);
            if (seen.containsKey(normalized)) {
                block.isDuplicate = true;
                seen.get(normalized).isDuplicate = true;
                duplicatesGroups.computeIfAbsent(normalized, k -> new ArrayList<>()).add(block);
                if (!duplicatesGroups.get(normalized).contains(seen.get(normalized))) {
                    duplicatesGroups.get(normalized).add(seen.get(normalized));
                }
            } else {
                block.isDuplicate = false;
                seen.put(normalized, block);
            }
        }
        return duplicatesGroups;
    }
} 