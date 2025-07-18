import ast

# python_blocks.py
# Author: Syed Umer Ahmad
# Description: Extracts code blocks from Python files for duplicate detection.
#
# This script parses Python files and extracts code blocks for further analysis.

class CodeBlock(object):
    def __init__(self, name, start_line, end_line, code, language):
        self.name = name
        self.start_line = start_line
        self.end_line = end_line
        self.code = code
        self.language = language  # 'python' or 'java'
        self.is_duplicate = False

    def scan(self):
        if self.is_duplicate:
            print("Duplicate block in {}: {} ({}-{})".format(self.language, self.name, self.start_line, self.end_line))
            print(self.code.strip())
            print("------")

def find_block_end(start_index, lines):
    base_indent = len(lines[start_index]) - len(lines[start_index].lstrip())
    for i in range(start_index + 1, len(lines)):
        line = lines[i]
        if line.strip() == "":
            continue
        current_indent = len(line) - len(line.lstrip())
        if current_indent <= base_indent:
            return i
    return len(lines)

def extract_python_blocks(filename):
    with open(filename, 'r') as f:
        source = f.read()

    tree = ast.parse(source)
    lines = source.splitlines()
    blocks = []

    for node in ast.walk(tree):
        if isinstance(node, (ast.FunctionDef, ast.ClassDef, ast.For, ast.While, ast.If)):
            start = node.lineno - 1
            end = find_block_end(start, lines)
            block_code = "\n".join(lines[start:end])
            blocks.append(CodeBlock(node.name if hasattr(node, 'name') else type(node).__name__, start + 1, end, block_code, 'python'))

    return blocks
