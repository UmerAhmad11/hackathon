# duplicate_detection.py
# Author: Syed Umer Ahmad
# Description: Detects duplicate code blocks between Java and Python files.
#
# This script reads code blocks from Java and Python files, compares them, and writes duplicates to output files.
#
# Usage:
#   python duplicate_detection.py
#
# Dependencies: None

import re

def normalize_code(code):
    code = re.sub(r'//.*', '', code)
    code = re.sub(r'#.*', '', code)
    code = re.sub(r'/\*.*?\*/', '', code, flags=re.DOTALL)
    code = re.sub(r'\s+', '', code)
    code = code.lower()
    return code

def mark_duplicates(blocks):
    seen = {}
    duplicates_groups = {}

    for block in blocks:
        code_to_normalize = getattr(block, 'body_only', block.code)
        normalized = normalize_code(code_to_normalize)
        if normalized in seen:
            block.is_duplicate = True
            seen[normalized].is_duplicate = True
            duplicates_groups.setdefault(normalized, []).append(block)
            if seen[normalized] not in duplicates_groups[normalized]:
                duplicates_groups[normalized].append(seen[normalized])
        else:
            block.is_duplicate = False
            seen[normalized] = block
    return duplicates_groups
