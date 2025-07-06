import re
from python_blocks import CodeBlock  # Import the class from python_blocks or move CodeBlock to a separate file

# java_blocks.py
# Author: Syed Umer Ahmad
# Description: Extracts code blocks from Java files for duplicate detection.
#
# This script parses Java files and extracts code blocks for further analysis.

class JavaNode(object):
    def __init__(self, node_type, name, start_line, end_line, body):
        self.type = node_type
        self.name = name
        self.start_line = start_line
        self.end_line = end_line
        self.body = body

def extract_block(lines, start):
    body = [lines[start]]
    brace_count = lines[start].count('{') - lines[start].count('}')
    i = start + 1
    while i < len(lines) and brace_count > 0:
        line = lines[i]
        body.append(line)
        brace_count += line.count('{') - line.count('}')
        i += 1
    return body, start, i - 1  # body as list, start and end indices (inclusive)

def parse_lines(lines, abs_start_idx):
    nodes = []
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        # Match class
        m = re.match(r'(public|private|protected)?\s*class\s+(\w+)', line)
        if m:
            name = m.group(2)
            block_lines, block_start, block_end = extract_block(lines, i)
            node = JavaNode("class", name, abs_start_idx + block_start + 1, abs_start_idx + block_end + 1, "".join(block_lines))
            nodes.append(node)
            if len(block_lines) > 1:
                sub_body_start = i + 1
                sub_body_end = block_end + 1
                subnodes, _ = parse_lines(lines[sub_body_start:sub_body_end], abs_start_idx + sub_body_start)
                nodes.extend(subnodes)
            i = block_end + 1
            continue
        # Match method
        m = re.match(r'(public|private|protected)?\s*(static)?\s*\w+\s+(\w+)\s*\(.*\)\s*{', line)
        if m:
            name = m.group(3)
            block_lines, block_start, block_end = extract_block(lines, i)
            node = JavaNode("method", name, abs_start_idx + block_start + 1, abs_start_idx + block_end + 1, "".join(block_lines))
            nodes.append(node)
            i = block_end + 1
            continue
        # Match control blocks
        for keyword in ['if', 'else if', 'else', 'for', 'while', 'do']:
            pattern = r'^' + keyword + r'\b.*{'
            if re.match(pattern, line):
                name = keyword.upper() + "_BLOCK_" + str(abs_start_idx + i + 1)
                block_lines, block_start, block_end = extract_block(lines, i)
                node = JavaNode("control", name, abs_start_idx + block_start + 1, abs_start_idx + block_end + 1, "".join(block_lines))
                nodes.append(node)
                i = block_end + 1
                break
        else:
            i += 1
    return nodes, i

def extract_java_blocks(filename):
    with open(filename, 'r') as f:
        lines = f.readlines()
    nodes, _ = parse_lines(lines, 0)
    blocks = []
    for node in nodes:
        block_lines = node.body.splitlines()
        if node.type == "method" and len(block_lines) > 1:
            body_only = "\n".join(block_lines[1:])
            for offset, line in enumerate(block_lines[1:], 1):
                if line.strip():
                    body_start_offset = offset
                    break
            else:
                body_start_offset = 1
            body_start_line = node.start_line + body_start_offset
            body_end_line = node.end_line
        else:
            body_only = "\n".join(block_lines)
            body_start_line = node.start_line
            body_end_line = node.end_line
        cb = CodeBlock(node.name, body_start_line, body_end_line, node.body, 'java')
        cb.body_only = body_only
        blocks.append(cb)
    return blocks
