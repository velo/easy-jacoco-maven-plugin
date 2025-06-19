# Basic Example of Easy JaCoCo Maven Plugin

This example demonstrates how to use the [Easy JaCoCo Maven Plugin](https://github.com/velo/easy-jacoco-maven-plugin) in a basic multi-module Maven project. It shows you how to set up the project, run unit tests, and generate a code coverage report while excluding specific packages from coverage.

> **Technical Limitation:** Because this example is also used for integration testing, you **must** include the property `-Deasy-jacoco.version=0.1.1` when running the build. (This is required to resolve the plugin version.)  
> **Note:** It is possible to remove this requirement by replacing all `${easy-jacoco.version}` references with the latest released version (e.g., `0.1.1`) in the example's `extensions.xml` and `pom.xml`.

## Overview

In this example:
- The project is defined with a parent POM and a child module ("module-1").
- A sample class (`SimpleMath.java`) and its tests (`SimpleMathTest.java`) illustrate simple arithmetic operations.
- The plugin is activated via an `extensions.xml` file, automatically activating the plugin during the Maven build.
- Coverage rules are configured to exclude classes under the package `com.marvinformatics.jacoco.easy_jacoco_maven_plugin`.
- Integration testing requires passing `-Deasy-jacoco.version=0.1.1` for proper plugin resolution. (You can remove this requirement by hardcoding the version in your configuration files.)

## Prerequisites

- **Java:** Version 11 or later (ensure `JAVA_HOME` is set appropriately)
- **Maven:** Version 3.9.x or compatible

## How to Run the Example

1. **Navigate to the Example Directory:**

   Open a terminal and change to the `examples/basic` directory:
   ```bash
   cd examples/basic
   ```

2. **Run the Maven Build:**

   Execute a Maven clean install. **Remember to include the mandatory property:**
   ```bash
   mvn clean install -Deasy-jacoco.version=0.1.1
   ```
   This command will:
   - Compile the project.
   - Run the unit tests.
   - Generate a JaCoCo coverage report.

## What to Expect

- **Compilation:** The project compiles without errors.
- **Unit Tests:** All tests (for instance, in `SimpleMathTest.java`) run and pass successfully.
- **Coverage Report:** A JaCoCo coverage report is generated according to the plugin configuration.
- **Coverage Check:** The `check-project` goal executes, enforcing your coverage rules while excluding the specified package.

## Customization

Feel free to modify the parent or module POMs, adjust the exclusion patterns, or update the plugin configuration to suit your project's needs. When a new version of the plugin is released, simply update the property value (`-Deasy-jacoco.version=...`) or remove it entirely by hardcoding the version in `extensions.xml` and the POM files.

Happy testing and enjoy the simplicity of zero-configuration coverage with the Easy JaCoCo Maven Plugin!
