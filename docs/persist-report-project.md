# Persist Report Project Goal

**Goal Name:** `persist-report-project`

**Phase:** `validate`

**Description:**  
The `persist-report-project` goal is used internally by the Easy JaCoCo plugin's lifecycle participant to persist a generated POM file required for aggregated reporting. This setup enables the later execution of project-wide reporting goals without any additional user configuration. It is not intended for direct use by end users.

**Usage:**  
This goal is automatically executed during the `validate` phase as part of the plugin's internal operations. Typically, you won't need to modify or invoke this goal manually.

**Parameters:**

- **mavenVersion** (String, Optional)  
  The version of Maven in use, typically for informational purposes.
  
- **projectDescription** (String, Optional)  
  A description for the aggregated report project.
  
- **projectName** (String, Optional)  
  The name of the aggregated report project.
  
- **reportArtifactId** (String, Optional)  
  The artifact ID to be used for the report project.
  
- **reportGroupId** (String, Optional)  
  The group ID for the report project.
  
- **reportVersion** (String, Optional)  
  The version for the report project.
  
- **session** (org.apache.maven.execution.MavenSession, Optional)  
  The Maven session object.
  
- **skip** (Boolean, Optional)  
  Skip execution of this goal.  
  *Default:* `false`.

**Example Configuration:**

Typically, no manual configuration is needed for this goal. It is part of the internal build lifecycle.
