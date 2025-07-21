# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Easy JaCoCo Maven Plugin is a Maven extension that simplifies JaCoCo code coverage setup in multi-module projects. It automatically gathers coverage data from all modules into aggregated reports and provides project-wide coverage checks with minimal configuration.

## Build and Development Commands

### Standard Build Commands
- **Full build**: `mvn clean install`
- **Quick build (skip tests and checks)**: `mvn clean install -Pquickbuild`
- **CI build**: `mvn clean install -Pci -Dgpg.skip=true`
- **Development build with formatting**: `mvn clean install -Pdev`

### Test Commands
- **Unit tests only**: `mvn test`
- **Integration tests**: `mvn failsafe:integration-test failsafe:verify`
- **Single test**: `mvn test -Dtest=ClassNameTest`

### Code Quality Commands
- **Format code**: `mvn git-code-format:format-code -Pdev`
- **Validate code format**: `mvn git-code-format:validate-code-format`
- **Sort POM**: `mvn sortpom:sort -Pdev`
- **License check**: `mvn license:check`
- **License format**: `mvn license:format -Pdev`

### Coverage Commands
- **Project coverage check**: `mvn com.marvinformatics.jacoco:easy-jacoco-maven-plugin:0.0.1-SNAPSHOT:check-project`
- **Project coverage report**: `mvn com.marvinformatics.jacoco:easy-jacoco-maven-plugin:0.0.1-SNAPSHOT:report-project`

## Architecture

### Core Components

**EasyJacocoLifecycleParticipant**: Maven lifecycle participant that automatically configures JaCoCo across all modules in a multi-module project. Activated via `.mvn/extensions.xml`.

**Maven Goals (Mojos)**:
- `ProjectReportMojo` (`report-project`): Generates aggregated coverage reports for the entire project
- `ProjectCheckMojo` (`check-project`): Performs project-wide coverage validation against configured rules
- `InstrumentJarMojo` (`instrument-jar`): Instruments JAR files for offline coverage collection
- `PersistProjectForReportMojo` (`persist-report-project`): Internal goal for lifecycle management

**JaCoCo Integration Layer** (`jacoco/` package):
- `ReportSupport`: Core reporting functionality 
- `CoverageBuilder`: Coverage data aggregation
- `FileFilter`: File inclusion/exclusion logic
- `ReportFormat`: Output format handling

### Plugin Activation

The plugin is activated as a Maven extension via `.mvn/extensions.xml`:
```xml
<extension>
  <groupId>com.marvinformatics.jacoco</groupId>
  <artifactId>easy-jacoco-maven-plugin</artifactId>
  <version>${easy-jacoco.version}</version>
</extension>
```

### Key Configuration Points

- **Skip execution**: `-Deasyjacoco.skip=true`
- **Example projects**: Located in `examples/` directory with working configurations
- **Integration tests**: Uses Maven Invoker for testing against real projects in `src/test/java`

## Development Environment

- **Java Version**: 11+ (configured in pom.xml as `main.java.version`)
- **Maven Version**: 3.9.9+ required
- **Code Formatting**: Google Java Format via git-code-format-maven-plugin
- **License**: Apache 2.0 (automatically applied to source files)

## Testing Strategy

The project uses both unit tests and integration tests:
- **Unit tests**: Standard JUnit 5 tests in `src/test/java`
- **Integration tests**: Maven Invoker-based tests that build and test real example projects
- **Example projects**: Located in `examples/` directory serve as both documentation and integration test subjects