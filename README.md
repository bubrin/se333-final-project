# se333-final-project


# MCP Tool API Documentation
## Test Generation
---------------
generate_tests(java_file):	                        Generates JUnit test stubs with basic assertions for all public methods
generate_boundary_tests(java_file):                 Creates JUnit test skeleton for boundary value testing
generate_equivalence_class_tests(java_file)	        Creates JUnit test skeleton for equivalence class testing
generate_decision_table_tests(java_file)	        Creates JUnit test skeleton for decision table testing
generate_contract_tests(java_file)	                Creates JUnit test skeleton for contract-based testing

Build & Test
------------
run_maven_tests(project_path)	                    Runs mvn clean test and returns output
parse_results(project_path)	                        Parses Maven Surefire reports and JaCoCo coverage XML
run_checkstyle(project_path)	                    Runs Maven Checkstyle and returns report path

Git Automation
--------------
git_status()	                                    Shows git status
git_add_all()	                                    Stages all changes except ignored files
git_commit(message)	                                Commits staged changes with optional coverage info
git_push(remote)	                                Pushes commits to the remote repository
git_pull_request(title, body, base)	                   Creates a GitHub pull request