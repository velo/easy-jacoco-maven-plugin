# Help Goal

**Goal Name:** `help`

**Description:**  
The `help` goal displays detailed help information about the Easy JaCoCo Maven Plugin, including available parameters for each goal. It mimics the standard Maven plugin help functionality, enabling users to review configuration options and usage details.

**Usage:**  
Invoke the help goal directly to get an overview or detailed information for a specific goal. For example:

```
mvn easy-jacoco:help -Ddetail=true -Dgoal=check-project
```

This command displays detailed parameter information for the `check-project` goal.

**Parameters:**

- **detail** (Boolean, Optional)  
  Set to `true` to display all settable properties for each goal.  
  *Default:* `false`.

- **goal** (String, Optional)  
  The name of the goal to get detailed help for.  
  *Default:* If unspecified, help for all goals is displayed.

- **indentSize** (Integer, Optional)  
  Number of spaces per indentation level in the output.  
  *Default:* `2`.

- **lineLength** (Integer, Optional)  
  Maximum length of each display line.  
  *Default:* `80`.

For further help on Maven plugin configuration, consult the [Maven Plugin Developers Guide](https://maven.apache.org/plugin-developers/index.html).
