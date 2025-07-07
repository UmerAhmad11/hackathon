import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import java.io.*;
import java.util.*;

public class JavaParserDuplicateDetection {
    public static class Block {
        String type;
        String name;
        int startLine;
        int endLine;
        String code;  // Normalized code

        public Block(String type, String name, int startLine, int endLine, String code) {
            this.type = type;
            this.name = name;
            this.startLine = startLine;
            this.endLine = endLine;
            this.code = normalizeCode(code);
        }

        private String normalizeCode(String code) {
            return code.replaceAll("//.*?\\n", "")      // remove single-line comments
                       .replaceAll("/\\*.*?\\*/", "")   // remove block comments
                       .replaceAll("\\s+", "");         // remove all whitespace
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Block)) return false;
            Block other = (Block) obj;
            return this.code.equals(other.code);
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java JavaParserDuplicateDetection <file1.java> <file2.java> ...");
            return;
        }

        List<Block> allBlocks = new ArrayList<>();
        for (String filename : args) {
            CompilationUnit cu = StaticJavaParser.parse(new File(filename));
            // Classes
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
                allBlocks.add(new Block("Class", cls.getNameAsString(),
                        cls.getBegin().get().line, cls.getEnd().get().line, cls.toString()));
                // Methods
                cls.findAll(MethodDeclaration.class).forEach(method -> {
                    allBlocks.add(new Block("Method", method.getNameAsString(),
                            method.getBegin().get().line, method.getEnd().get().line, method.getBody().map(Object::toString).orElse("")));
                });
            });
            // Top-level methods (if any)
            cu.findAll(MethodDeclaration.class).stream()
                .filter(m -> !m.findAncestor(ClassOrInterfaceDeclaration.class).isPresent())
                .forEach(method -> {
                    allBlocks.add(new Block("Method", method.getNameAsString(),
                            method.getBegin().get().line, method.getEnd().get().line, method.getBody().map(Object::toString).orElse("")));
                });
        }

        // Group by normalized code
        Map<String, List<Block>> duplicates = new HashMap<>();
        for (Block block : allBlocks) {
            duplicates.computeIfAbsent(block.code, k -> new ArrayList<>()).add(block);
        }

        // Print duplicates
        for (List<Block> group : duplicates.values()) {
            if (group.size() > 1) {
                System.out.println("Duplicate block found:");
                for (Block b : group) {
                    System.out.printf("  -> %s %s [%d-%d]\n", b.type, b.name, b.startLine, b.endLine);
                }
                System.out.println("------");
            }
        }
    }
} 