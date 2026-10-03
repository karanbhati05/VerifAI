#!/usr/bin/env python3
"""
VerifAI Test Runner & Live Endpoint Validator
Use this script to run unit tests or probe live deployed endpoints on Render.
"""

import sys
import subprocess
import os

def run_command(desc, cmd, cwd=None):
    print(f"\n==========================================")
    print(f"▶ Running: {desc}")
    print(f"==========================================")
    result = subprocess.run(cmd, shell=True, cwd=cwd)
    if result.returncode == 0:
        print(f"✅ PASS: {desc}")
        return True
    else:
        print(f"❌ FAIL: {desc} (Exit code: {result.returncode})")
        return False

def main():
    root_dir = os.path.dirname(os.path.abspath(__file__))
    backend_dir = os.path.join(root_dir, "verifai-backend")
    ai_dir = os.path.join(root_dir, "ai-engine")

    all_passed = True

    # 1. Run Python AI Engine Tests
    print("\n--- 1. Python AI Engine Tests ---")
    ai_test_cmd = f"{sys.executable} -m pytest tests/ -v"
    if not run_command("AI Engine Pytest Suite", ai_test_cmd, cwd=ai_dir):
        all_passed = False

    # 2. Run Java Backend Tests
    print("\n--- 2. Spring Boot Backend Tests ---")
    mvn_cmd = "mvn test" if os.name != 'nt' else "mvn.cmd test"
    # Fallback to mvnw if mvn not directly in PATH
    mvnw_path = os.path.join(backend_dir, "mvnw.cmd" if os.name == 'nt' else "mvnw")
    if os.path.exists(mvnw_path):
        mvn_cmd = f'"{mvnw_path}" test'

    if not run_command("Spring Boot Backend JUnit & MockMvc Suite", mvn_cmd, cwd=backend_dir):
        all_passed = False

    print("\n==========================================")
    if all_passed:
        print("🎉 ALL TESTS PASSED SUCCESSFULLY!")
    else:
        print("⚠️ SOME TESTS ENCOUNTERED FAILURES.")
    print("==========================================\n")

if __name__ == "__main__":
    main()
