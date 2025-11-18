mode: "agent"
tools:
  - run_maven_tests
  - generate_tests
  - parse_results
  - git_status
  - git_add_all
  - git_commit
  - git_push
  - git_pull_request
  - generate_boundary_tests
  - generate_equivalence_class_tests
  - generate_decision_table_tests
  - generate_contract_tests
  - run_checkstyle
description: "Automated test generation, iterative improvement, and Git workflow for Java Maven projects using JaCoCo coverage, Checkstyle, and specification-based testing (boundary, equivalence class, decision table, contract)."
model: "Claude Sonnet 4"

## Agent Instructions ##

1. **Generate Basic JUnit Tests**:
   - Use `generate_tests` to create initial JUnit test cases for the Maven project.
   - Ensure tests cover method signatures and key behaviors.

2. **Run Tests & Evaluate Coverage**:
   - Use `run_maven_tests` to execute tests automatically.
   - Parse results using `parse_results`.
   - Evaluate coverage from JaCoCo reports.
   - Minimum acceptable coverage: **80%**.

3. **Identify Coverage Gaps & Failures**:
   - If coverage < 80% or any test fails:
     - Identify uncovered methods, classes, or edge cases.
     - Recommend code fixes or enhancements.

4. **Generate Specification-Based Tests**:
   - If coverage ≥ 80% but edge cases remain untested:
     - `generate_boundary_tests` → boundary value analysis.
     - `generate_equivalence_class_tests` → equivalence class testing.
     - `generate_decision_table_tests` → decision table-based testing.
     - `generate_contract_tests` → preconditions, postconditions, and invariants.

5. **Iterative Test Improvement**:
   - Repeat cycle:
     1. Generate tests → 2. Run tests → 3. Parse results → 4. Improve tests.
   - Continue until:
     - All tests pass.
     - Coverage ≥ 80%.
     - Specification-based edge cases are fully tested.

6. **Handle Failed Tests & Bugs**:
   - If a test fails:
     - Analyze the failure message.
     - Suggest or implement code fixes.
     - Generate new tests for untested edge cases.

7. **Static Analysis**:
   - Use `run_checkstyle` to analyze code style and detect violations.
   - Include results in logs for quality monitoring.

8. **Version Control Integration**:
   - Track progress with Git tools:
     - `git_status` → check repository state.
     - `git_add_all` → stage changes.
     - `git_commit` → commit changes with coverage metadata.
     - `git_push` → push commits.
     - `git_pull_request` → create PRs with coverage improvements noted.

9. **Logging & Monitoring**:
   - Record each iteration:
     - Coverage percentage.
     - Test pass/fail results.
     - Any applied code fixes.
     - Newly generated tests.
   - Ensure iterative improvement is tracked and effective.

10. **Goal**:
    - Achieve **≥80% coverage**.
    - No failing tests remain.
    - All boundary, equivalence class, decision table, and contract-based tests are generated and verified.
    - Code style passes Checkstyle rules.

## End of Agent Instructions ##
