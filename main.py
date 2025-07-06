import os
import threading

from python_blocks import extract_python_blocks
from java_blocks import extract_java_blocks
from duplicate_detection import mark_duplicates, normalize_code
import time
start_time = time.time()
# main.py
# Author: Syed Umer Ahmad
# Description: Main entry point for duplicate detection between Java and Python codebases.
#
# This script orchestrates the process of reading, comparing, and reporting duplicate code blocks.

def run_in_threads(blocks):
    threads = []
    for block in blocks:
        t = threading.Thread(target=block.scan)
        threads.append(t)
        t.start()
    for t in threads:
        t.join()

def extract_blocks(filename):
    if filename.endswith('.py'):
        return extract_python_blocks(filename)
    elif filename.endswith('.java'):
        return extract_java_blocks(filename)
    else:
        print("Unsupported file type:", filename)
        return []

if __name__ == "__main__":
    # Use the new tests directory for input files
    filenames = ["tests/EmployeeManager1.java", "tests/python_test.py"]
    all_blocks = []
    
    for file in filenames:
        if os.path.exists(file):
            all_blocks.extend(extract_blocks(file))
        else:
            print("File not found:", file)

    duplicates = mark_duplicates(all_blocks)

    # Initialize report data
    python_report_lines = []
    java_report_lines = []

    if duplicates:
        print("Duplicate blocks found:\n")
        for group in duplicates.values():
            for block in group:
                if getattr(block, 'is_duplicate', False):
                    entry = (
                        "Duplicate block in {}: {} ({}-{})\n".format(block.language, block.name, block.start_line, block.end_line)
                        + "{}\n------\n".format(block.code.strip())
                    )
                    print(entry, end="")
                    if block.language == 'python':
                        python_report_lines.append(entry)
                    elif block.language == 'java':
                        java_report_lines.append(entry)
    else:
        print("No duplicate blocks found.")
        python_report_lines.append("No duplicate blocks found.\n")
        java_report_lines.append("No duplicate blocks found.\n")

    # Write to separate files in the output directory
    with open("output/duplicates_python.txt", "w") as f:
        f.writelines(python_report_lines)

    with open("output/duplicates_java.txt", "w") as f:
        f.writelines(java_report_lines)

    # Write all detected Python blocks to output/python_outtie (deduplicated)
    seen = set()
    with open("output/python_outtie", "w") as f:
        for block in all_blocks:
            if getattr(block, 'language', None) == 'python':
                code_to_normalize = getattr(block, 'body_only', block.code)
                normalized = normalize_code(code_to_normalize)
                if normalized in seen:
                    continue
                seen.add(normalized)
                entry = (
                    "Python block: {} ({}-{})\n".format(block.name, block.start_line, block.end_line)
                    + "{}\n------\n".format(block.code.strip())
                )
                f.write(entry)

    # Optional: run threaded scanning print
    run_in_threads(all_blocks)

    # Calculate and report duplication scores for Python and Java separately
    total_python_blocks = sum(1 for block in all_blocks if getattr(block, 'language', None) == 'python')
    total_java_blocks = sum(1 for block in all_blocks if getattr(block, 'language', None) == 'java')
    duplicate_python_blocks = sum(1 for block in all_blocks if getattr(block, 'is_duplicate', False) and getattr(block, 'language', None) == 'python')
    duplicate_java_blocks = sum(1 for block in all_blocks if getattr(block, 'is_duplicate', False) and getattr(block, 'language', None) == 'java')

    if total_python_blocks > 0:
        python_duplication_score = float(duplicate_python_blocks) / total_python_blocks
    else:
        python_duplication_score = 0.0
    if total_java_blocks > 0:
        java_duplication_score = float(duplicate_java_blocks) / total_java_blocks
    else:
        java_duplication_score = 0.0

    score_report = (
        "Python Duplication Score: {:.2f} ({} duplicate blocks / {} total blocks)\n".format(python_duplication_score, duplicate_python_blocks, total_python_blocks) +
        "Java Duplication Score: {:.2f} ({} duplicate blocks / {} total blocks)\n".format(java_duplication_score, duplicate_java_blocks, total_java_blocks)
    )
    print(score_report)
    with open("output/duplication_score.txt", "w") as f:
        f.write(score_report)

end_time = time.time()

print("Runtime: {}".format(end_time-start_time))