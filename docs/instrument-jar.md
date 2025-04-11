# Instrument Jar Goal

**Goal Name:** `instrument-jar`

**Phase:** `package`

**Description:**  
The `instrument-jar` goal instruments jar files (or any specified files) to include coverage probes. In addition to utilizing the on-the-fly JaCoCo agent, this goal allows for offline instrumentation by processing files that end with `.exec` found anywhere in the project. This means you’re not limited to classpath files and can extend instrumentation to any relevant file.

**Usage:**  
This goal is typically executed during the `package` phase and produces instrumented files ready for coverage analysis.

**Parameters:**

- **destination** (File, Required)  
  The target file or directory for the instrumented output.

- **skip** (Boolean, Optional)  
  If set to `true`, instrumentation will be skipped.  
  *Default:* `false`.

- **source** (File, Required)  
  The source file (or jar) that will be instrumented.

**Example Configuration:**

```
<plugin>
  <groupId>com.marvinformatics.jacoco</groupId>
  <artifactId>easy-jacoco-maven-plugin</artifactId>
  <version>0.1</version>
  <configuration>
    <source>${project.build.directory}/original.jar</source>
    <destination>${project.build.directory}/instrumented.jar</destination>
    <skip>false</skip>
  </configuration>
</plugin>
```

For a similar offline instrumentation process, see the [JaCoCo instrument-mojo documentation](https://www.eclemma.org/jacoco/trunk/doc/instrument-mojo.html).
