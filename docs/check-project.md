# Check Project Goal

**Goal Name:** `check-project`

**Phase:** `verify`

**Description:**  
The `check-project` goal performs an aggregated coverage check across the entire project. It collects execution data from all modules and verifies that the project-wide coverage meets the thresholds specified in the `projectRules` configuration. This goal is analogous to the [JaCoCo check-mojo](https://www.eclemma.org/jacoco/trunk/doc/check-mojo.html) but operates on the complete project.

**Usage:**  
This goal is bound to the `verify` phase and is triggered automatically during the Maven build when Easy JaCoCo is activated. To customize the coverage checks, you can configure the plugin in your parent POM (see the README for details on optional configuration).

**Parameters:**

- **dataFileExcludes** (List, Optional)  
  A list of execution data files to exclude from the report. Supports wildcard characters (`*`, `?`).  
  *Default:* None.

- **dataFileIncludes** (List, Optional)  
  A list of execution data files to include from each module. Supports wildcards.  
  *Default:* All `*.exec` files in target directories.

- **excludes** (List, Optional)  
  A list of class files to exclude from the coverage check. Supports wildcards.  
  *Default:* None.

- **haltOnFailure** (Boolean, Required)  
  Whether to halt the build if the coverage check fails.  
  *Default:* `false` (warn only).

- **includes** (List, Optional)  
  A list of class files to include in the coverage check. Supports wildcards.  
  *Default:* All classes.

- **projectRules** (List, Required)  
  Check configuration used to specify rules on element types (BUNDLE, PACKAGE, CLASS, SOURCEFILE, or METHOD) along with limits.  
  *Default:* Uses JaCoCo defaults if not specified.

- **session** (org.apache.maven.execution.MavenSession, Optional)  
  Maven session object provided by the Maven runtime.

- **skip** (Boolean, Optional)  
  Skip the execution of this goal.  
  *Default:* `false`.

**Example Configuration:**

```
<plugin>
  <groupId>com.marvinformatics.jacoco</groupId>
  <artifactId>easy-jacoco-maven-plugin</artifactId>
  <version>0.1</version>
  <configuration>
    <projectRules>
      <rule>
        <element>BUNDLE</element>
        <limits>
          <limit>
            <counter>LINE</counter>
            <value>COVEREDRATIO</value>
            <minimum>0.70</minimum>
          </limit>
        </limits>
      </rule>
    </projectRules>
    <haltOnFailure>true</haltOnFailure>
  </configuration>
</plugin>
```

For further details, see the [JaCoCo check-mojo documentation](https://www.eclemma.org/jacoco/trunk/doc/check-mojo.html).
