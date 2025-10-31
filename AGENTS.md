# AGENTS.md

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
- **Integration tests**: `mvn clean install -Pdev` (integration tests require prior install)
- **Single test**: `mvn test -Dtest=ClassNameTest`
- **Note**: Integration tests (ending in IT) will NOT work with `mvn test` - they must be run via `mvn clean install -Pdev`

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

**EasyJacocoLifecycleParticipant** (src/main/java/com/marvinformatics/easyjacoco/EasyJacocoLifecycleParticipant.java:48):
Maven lifecycle participant that hooks into the build lifecycle at `afterProjectsRead`. Key responsibilities:
- Registers vanilla JaCoCo plugin on all modules with `prepare-agent`, `prepare-agent-integration`, `report`, and `report-integration` goals
- For multi-module projects, dynamically generates a coverage aggregation POM in configurable directory (default: `coverage/`)
- The generated POM can be updated incrementally - existing pom.xml files are merged, not overwritten
- Auto-skips non-modular projects with a warning message (since 0.0.1-SNAPSHOT)
- Configurable via `coverageProjectDir` (default: "coverage") and `overrideDependencies` (default: false) parameters

**Maven Goals (Mojos)**:
- `ProjectReportMojo` (`report-project`): Generates aggregated coverage reports (HTML, XML, CSV) by merging all `*.exec` files from the project
- `ProjectCheckMojo` (`check-project`): Performs project-wide coverage validation against configured rules with default 80% instruction coverage
- `InstrumentJarMojo` (`instrument-jar`): Instruments JAR files for offline coverage collection
- `PersistProjectForReportMojo` (`persist-report-project`): Internal goal that persists report project metadata to the generated POM

**JaCoCo Integration Layer** (src/main/java/com/marvinformatics/easyjacoco/jacoco/):
- `ReportSupport`: Core reporting functionality - manages JaCoCo report visitors and coverage data aggregation
- `CoverageBuilder`: Coverage data aggregation from class files and execution data
- `FileFilter`: File inclusion/exclusion logic using wildcards for both source files and execution data
- `ReportFormat`: Output format handling (HTML, XML, CSV)

### Key Architectural Patterns

**Dynamic POM Generation**: Instead of requiring users to create a dedicated aggregator module, the plugin dynamically generates one at build time. This generated POM:
- Lives in a configurable directory (default: `coverage/`)
- Declares dependencies on all reactor modules
- Is intelligently merged with existing files rather than overwritten
- Includes plugin execution for report and check goals

**Lifecycle Integration**: The plugin uses Maven's `AbstractMavenLifecycleParticipant` to inject itself early in the build process, allowing it to modify all module POMs before they're built.

**File Discovery**: Coverage data (`.exec` files) and class files are discovered using flexible glob patterns (`**/target/*.exec` by default), making the plugin work with varied project structures.

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
- **Coverage directory**: Configure via `<coverageProjectDir>` in plugin config (default: "coverage")
- **Dependency handling**: Set `<overrideDependencies>true</overrideDependencies>` to replace rather than merge dependencies
- **Example projects**: Located in `examples/` directory with working configurations
- **Integration tests**: Uses Maven Invoker pattern in `ExampleProjectMavenBuildIT.java` to test against real projects

## Development Environment

- **Java Version**: 11+ (configured in pom.xml as `main.java.version`)
- **Maven Version**: 3.9.9+ required
- **Code Formatting**: Google Java Format via git-code-format-maven-plugin
- **License**: Apache 2.0 (automatically applied to source files, except `jacoco/` package which is adapted from JaCoCo)

## Testing Strategy

The project uses both unit tests and integration tests:
- **Unit tests**: Standard JUnit 5 tests in `src/test/java`
- **Integration tests**: `ExampleProjectMavenBuildIT.java` uses Maven Invoker to build and verify real example projects
- **Example projects**: `examples/basic/` and `examples/instrument-jar/` serve as both documentation and integration test subjects
- **Important**: The plugin must be installed (`mvn clean install`) before integration tests can run successfully