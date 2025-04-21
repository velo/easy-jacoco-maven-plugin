# Report Project Goal

**Goal Name:** `report-project`

**Phase:** `verify`

**Description:**  
The `report-project` goal generates an aggregated coverage report for the entire project by merging execution data from all modules into a single report. It works similarly to the [JaCoCo report-aggregate mojo](https://www.eclemma.org/jacoco/trunk/doc/report-aggregate-mojo.html) but without requiring a separate aggregator project or manual dependency management.

**Usage:**  
This goal is automatically bound to the `verify` phase when Easy JaCoCo is activated. The aggregated report is produced in multiple formats (HTML, XML, CSV) and is placed under the directory defined by `${project.build.directory}/jacoco-project-report`.

**Parameters:**

- **dataFileIncludes** (List, Optional)
  List of execution data files to include from each module.
  *Default:* All `*.exec` files in target directories.

- **dataFileExcludes** (List, Optional)
  List of execution data files to exclude from the report. Supports wildcards.
  *Default:* None.

- **includes** (List, Optional)
  List of class files to include in the report. Supports wildcards.
  *Default:* All classes.

- **excludes** (List, Optional)
  List of class files to exclude from the report. Supports wildcards.
  *Default:* None.

- **excludeModules** (List, Optional)
  A list of module artifactIds to exclude from the coverage check.
  *Default:* None.

- **footer** (String, Optional, since 0.7.7)
  Footer text to be displayed in the HTML report pages.
  *Default:* None.

- **formats** (List, Optional, since 0.8.7)
  Report formats to generate. Supported: `HTML`, `XML`, `CSV`.
  *Default:* `HTML, XML, CSV`.

- **outputDirectory** (File, Optional)
  Directory where the reports will be generated.
  *Default:* `${project.build.directory}/jacoco-project-report`.

- **outputEncoding** (String, Optional)
  Encoding for the generated reports.
  *Default:* `UTF-8` (or `${project.reporting.outputEncoding}`).

- **skip** (Boolean, Optional)
  Skip the execution of this goal.
  *Default:* `false`.

- **sourceEncoding** (String, Optional)
  Source file encoding.
  *Default:* `UTF-8` (or `${project.build.sourceEncoding}`).

- **title** (String, Optional, since 0.7.7)
  Title for the root node in HTML report pages.
  *Default:* `${project.name}`.

**Example Configuration:**

```
<plugin>
  <groupId>com.marvinformatics.jacoco</groupId>
  <artifactId>easy-jacoco-maven-plugin</artifactId>
  <version>0.1</version>
  <configuration>
    <formats>HTML,XML,CSV</formats>
    <outputDirectory>${project.reporting.outputDirectory}/jacoco-aggregate</outputDirectory>
    <outputEncoding>UTF-8</outputEncoding>
    <sourceEncoding>${project.build.sourceEncoding}</sourceEncoding>
    <title>${project.name}</title>
  </configuration>
</plugin>
```

For more details, refer to the [JaCoCo report-aggregate documentation](https://www.eclemma.org/jacoco/trunk/doc/report-aggregate-mojo.html).
