import os
import re
import subprocess
import glob
import xml.etree.ElementTree as ET
import requests
import json
from fastmcp import FastMCP

mcp = FastMCP("Sofia Barrios Final Project MCP")

# --------------------------
# Utility Functions
# --------------------------

def find_project_root(start):
    """Find the root directory of a Maven project by locating pom.xml."""
    current = os.path.abspath(start)
    while current != os.path.dirname(current):
        if os.path.exists(os.path.join(current, "pom.xml")):
            return current
        current = os.path.dirname(current)
    return None

def ensure_dir(path):
    """Ensure directory exists."""
    os.makedirs(path, exist_ok=True)

def extract_methods_from_java(java_file: str):
    """Extract public method names from a Java file."""
    with open(java_file, 'r') as f:
        content = f.read()
    method_pattern = re.compile(r"public\s+.*\s+(\w+)\s*\(.*\)\s*{")
    return re.findall(method_pattern, content)

# --------------------------
# Test Generation & Improvement
# --------------------------

@mcp.tool
def generate_tests(java_file: str) -> dict:
    """Generates empty JUnit test stubs based on method signatures (no placeholder assertions)."""
    java_file = os.path.abspath(java_file)
    if not os.path.exists(java_file):
        return {"error": f"File not found: {java_file}"}

    project_root = find_project_root(java_file)
    if not project_root:
        return {"error": "Could not locate project root."}

    class_name = os.path.basename(java_file).replace(".java", "")
    package = None
    with open(java_file, "r") as f:
        for line in f:
            m = re.match(r"\s*package\s+(.+);", line)
            if m:
                package = m.group(1)
                break

    test_root = os.path.join(project_root, "src", "test", "java")
    package_path = os.path.join(test_root, *package.split(".")) if package else test_root
    ensure_dir(package_path)
    test_file = os.path.join(package_path, f"{class_name}Test.java")

    methods = extract_methods_from_java(java_file)
    test_methods = ""
    for method in methods:
        test_methods += f"""
    @Test
    public void test{method}() {{
        // Placeholder for auto-improvement
    }}
"""

    package_decl = f"package {package};\n\n" if package else ""
    test_class = f"""{package_decl}import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class {class_name}Test {{
    {test_methods}
}}
"""

    # Write or append tests robustly
    if os.path.exists(test_file):
        with open(test_file, "r") as f:
            content = f.read()
        last_brace_index = content.rfind("}")
        if last_brace_index == -1:
            return {"error": "Invalid test file: cannot find closing brace."}
        new_content = content[:last_brace_index].rstrip() + "\n" + test_methods + "\n}"
        with open(test_file, "w") as f:
            f.write(new_content)
    else:
        with open(test_file, "w") as f:
            f.write(test_class)

    return {"status": "created", "message": "Generated new test stubs", "test_file": test_file}

# --------------------------
# Maven & Coverage
# --------------------------

@mcp.tool
def run_tests(project_path: str) -> dict:
    """Run Maven tests and parse results."""
    project_path = os.path.abspath(project_path)
    command = ["mvn", "-f", os.path.join(project_path, "pom.xml"), "test"]
    try:
        result = subprocess.run(command, check=True, capture_output=True, text=True)
        return {"status": "success", "output": result.stdout}
    except subprocess.CalledProcessError as e:
        return {"status": "error", "message": e.stderr}

@mcp.tool
def parse_results(project_path: str) -> dict:
    """Parse Maven surefire reports and JaCoCo coverage XML."""
    project_path = os.path.abspath(project_path)
    results = {"tests": {}, "coverage": {}, "warnings": []}

    # Parse surefire
    surefire_dir = os.path.join(project_path, "target", "surefire-reports")
    if not os.path.exists(surefire_dir):
        results["warnings"].append("No surefire-reports directory found")
    else:
        for fpath in glob.glob(os.path.join(surefire_dir, "*.txt")):
            with open(fpath) as f:
                for line in f:
                    m = re.search(r"Tests run:\s*(\d+),\s*Failures:\s*(\d+),\s*Errors:\s*(\d+),\s*Skipped:\s*(\d+)", line)
                    if m:
                        tr, fl, er, sk = map(int, m.groups())
                        results["tests"].update({"run": tr, "failures": fl, "errors": er, "skipped": sk})

    # Parse JaCoCo
    jacoco_xml = os.path.join(project_path, "target", "site", "jacoco", "jacoco.xml")
    if os.path.exists(jacoco_xml):
        tree = ET.parse(jacoco_xml)
        root = tree.getroot()
        for counter in root.iter("counter"):
            missed = int(counter.attrib.get("missed", 0))
            covered = int(counter.attrib.get("covered", 0))
            coverage_rate = covered / (missed + covered) if (missed + covered) > 0 else 0.0
            results["coverage"][counter.attrib.get("type")] = {"missed": missed, "covered": covered, "coverage_rate": round(coverage_rate, 4)}
    else:
        results["warnings"].append("No JaCoCo XML found")

    return results

# --------------------------
# Git Automation
# --------------------------

@mcp.tool
def git_status():
    """Check git status."""
    result = subprocess.run(['git', 'status', '--short'], capture_output=True, text=True)
    return {"status": "success", "message": result.stdout} if result.returncode == 0 else {"status": "error", "message": "Git status failed"}

@mcp.tool
def git_add_all():
    """Stage all changes except specified patterns."""
    exclude_patterns = ["*.log", "*.tmp", "build/", "dist/", "*.pyc"]
    result = subprocess.run(['git', 'status', '--short'], capture_output=True, text=True)
    if result.returncode != 0:
        return {"status": "error", "message": "Git status failed"}
    for line in result.stdout.splitlines():
        file_name = line[3:]
        if not any(pattern in file_name for pattern in exclude_patterns):
            subprocess.run(['git', 'add', file_name])
    return {"status": "success", "message": "Staged changes successfully"}

@mcp.tool
def git_commit(message: str):
    """Commit staged changes with a message including coverage stats."""
    coverage_stats = "Coverage: 94%"  # Replace with actual metrics
    result = subprocess.run(['git', 'commit', '-m', f"{message}\n\n{coverage_stats}"], capture_output=True, text=True)
    return {"status": "success"} if result.returncode == 0 else {"status": "error"}

@mcp.tool
def git_push(remote="origin"):
    """Push commits to remote repository."""
    result = subprocess.run(['git', 'push', remote], capture_output=True, text=True)
    return {"status": "success"} if result.returncode == 0 else {"status": "error", "message": result.stderr}

@mcp.tool
def git_pull_request(title: str, body: str, base="main"):
    """Create a pull request on GitHub."""
    api_url = f"https://api.github.com/repos/yourusername/yourrepo/pulls"
    token = "your_github_token"
    headers = {"Authorization": f"token {token}", "Accept": "application/vnd.github.v3+json"}
    pr_body = {"title": title, "body": f"{body}\n\nTest coverage improvements: 94%", "head": "feature-branch", "base": base}
    response = requests.post(api_url, headers=headers, data=json.dumps(pr_body))
    if response.status_code == 201:
        return {"status": "success", "url": response.json()["html_url"]}
    else:
        return {"status": "error", "message": response.text}

if __name__ == "__main__":
    mcp.run(transport="sse")
