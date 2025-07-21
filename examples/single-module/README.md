# Single-Module Example

This example demonstrates the behavior of the Easy JaCoCo Maven Plugin when applied to a non-modular (single-module) Maven project.

## Expected Behavior

When this project is built with the Easy JaCoCo Maven Plugin extension enabled, the plugin will:

1. Detect that this is a single-module project (no `<modules>` section in the pom.xml)
2. Display a warning message explaining that the plugin is designed for multi-module projects
3. Skip execution gracefully, allowing the build to continue successfully

## Warning Message

You should see a log message similar to:

```
[WARNING] EasyJacoco detected a non-modular project. This plugin is designed for multi-module Maven projects and will be skipped. 
For single-module projects, consider using the standard jacoco-maven-plugin directly. 
If you believe this is incorrect, you can force execution with -Deasyjacoco.skip=false.
```

## Purpose

This example serves as:
- A test case for the auto-skip functionality
- Documentation of expected behavior for single-module projects
- Validation that the plugin handles edge cases gracefully

For actual JaCoCo coverage on single-module projects, use the standard [jacoco-maven-plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html) instead.