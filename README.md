# Sofia Barrios
# SE333 Final Project

## Project Description
This project implements an AI-assisted agent to
automatically generate, run, and iteratively improve JUnit tests
for Java Maven projects. It leverages specification-based testing
(boundary, equivalence class, decision table, contract) along
with Checkstyle and JaCoCo coverage analysis to ensure code
quality. The goal is to achieve at least 80% code coverage while
maintaining passing tests and enforcing coding standards. This
report summarizes methodology, coverage improvement patterns,
lessons learned, and future enhancements.
## Goals
- Achieve ≥ 80% code coverage
- Perform specification based testing
- Enforce coding standards with Checkstyle
- Generate checkstyle and jacoco reports


## Installation and Configuration
### Pre-reqs: Jacoco, Checkstyle, Maven, Java 11+ 
### 1. git clone <repo_url>
   cd se333-final-project
### 2. python -m venv .venv
   source .venv/Scripts/activate  # Windows PowerShell\
   **OR**\
   source .venv/bin/activate      # macOS/Linux\
   pip install -r requirements.txt\
### 3. Ensure MCP agent configuration
   Verify main.py is running:\
   python main.py\
### 4. Configure MCP servers in VSC
    Settings->MCP Tools->Servers, add:\
    {\
  "servers": {\
    "local-agent": {\
      "url": "http://127.0.0.1:8000/sse",\
      "type": "http"\
      }\
    }\
  }
## MCP Tool API Documentation
### Test Generation
---------------
generate_tests(java_file):	                        Generates JUnit test stubs with basic assertions for all public methods
generate_boundary_tests(java_file):                 Creates JUnit test skeleton for boundary value testing
generate_equivalence_class_tests(java_file)	        Creates JUnit test skeleton for equivalence class testing
generate_decision_table_tests(java_file)	        Creates JUnit test skeleton for decision table testing
generate_contract_tests(java_file)	                Creates JUnit test skeleton for contract-based testing

### Build & Test
------------
run_maven_tests(project_path)	                    Runs mvn clean test and returns output
parse_results(project_path)	                        Parses Maven Surefire reports and JaCoCo coverage XML
run_checkstyle(project_path)	                    Runs Maven Checkstyle and returns report path

### Git Automation
--------------
git_status()	                                    Shows git status
git_add_all()	                                    Stages all changes except ignored files
git_commit(message)	                                Commits staged changes with optional coverage info
git_push(remote)	                                Pushes commits to the remote repository
git_pull_request(title, body, base)	                   Creates a GitHub pull request

## Troubleshooting

MCP agent not responding:
Make sure the server URL in main.py matches the MCP server configuration. Check firewall/ports.

Tests fail to generate:
Ensure the Java file path is correct and the .java file compiles.

Maven build errors:
Run mvn clean install first. Check for duplicate test methods in test files.

Virtual environment issues:
Deactivate old environments with deactivate. Ensure the correct Python interpreter is selected in VS Code.
