#!/usr/bin/env python3
# debug_java_parsing.py
# Debug script to test Java parsing

from java_blocks import extract_java_blocks

def debug_java_parsing():
    """Debug the Java parsing to see what blocks are extracted."""
    filename = "tests/EmployeeManager1.java"
    blocks = extract_java_blocks(filename)
    
    print("=== Java Blocks Extracted ===")
    print("Total blocks found: {}".format(len(blocks)))
    print()
    
    for i, block in enumerate(blocks):
        print("Block {}: {} (lines {}-{})".format(i+1, block.name, block.start_line, block.end_line))
        print("Type: {}".format(getattr(block, 'type', 'unknown')))
        print("Code preview:")
        lines = block.code.split('\n')
        for j, line in enumerate(lines[:3]):  # Show first 3 lines
            print("  {}: {}".format(j+1, line))
        if len(lines) > 3:
            print("  ... ({} more lines)".format(len(lines) - 3))
        print("-" * 50)

if __name__ == "__main__":
    debug_java_parsing() 