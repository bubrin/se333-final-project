mode: "agent"
tools:
  - generate_tests
  - parse_results
  - improve_tests
  - generate_and_improve
  - git_status
  - git_add_all
  - git_commit
  - git_push
  - git_pull_request
description: "Automated test generation, improvement, and Git workflow for Java Maven projects using JaCoCo coverage."
model: "Gpt-5 mini"

## Agent Instructions ##

1. **Generate JUnit Tests**:
   - Use `generate_tests` or `generate_and_improve` to create test stubs for all public methods in Java classes.
   - Ensure initial tests cover method signatures and key behaviors.

2. **Run Tests & Evaluate Coverage**:
   - Use Maven to execute tests.
   - Parse results with `parse_results`.
   - Evaluate coverage using JaCoCo reports.
   - Minimum acceptable coverage: **80%**.

3. **Identify Coverage Gaps & Failures**:
   - If coverage < 80% or any test fails:
     - Identify uncovered methods, classes, or edge cases.
     - Recommend code fixes or enhancements.
     - Generate additional tests targeting these gaps using `improve_tests`.

4. **Iterative Test Improvement**:
   - Continue the cycle: generate tests → run tests → parse results → improve tests.
   - Repeat until:
     - All tests pass.
     - Coverage meets or exceeds 80%.

5. **Handle Failed Tests & Bugs**:
   - If a test fails, analyze the error message.
   - Suggest or implement code fixes.
   - Generate new tests if failure is due to untested edge cases.

6. **Version Control Integration**:
   - Use Git tools to track progress:
     - `git_status` → check repository status.
     - `git_add_all` → stage changes.
     - `git_commit` → commit changes with coverage metadata.
     - `git_push` → push commits to remote.
     - `git_pull_request` → create PRs with coverage improvements noted.

7. **Logging & Monitoring**:
   - Log each iteration of test generation and improvement.
   - Include:
     - Coverage percentage.
     - Test pass/fail results.
     - Any code fixes applied.
     - Newly generated tests.
   - Use logs to ensure iterative improvement is effective.

8. **Goal**:
   - Achieve **≥80% coverage**.
   - No failing tests remain.
   - Edge cases are tested and verified.

## End of Agent Instructions ##