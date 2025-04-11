# Easy JaCoCo Maven Plugin [![Buid project](https://github.com/velo/easy-jacoco-maven-plugin/actions/workflows/deploy.yml/badge.svg)](https://github.com/velo/easy-jacoco-maven-plugin/actions/workflows/deploy.yml)

**Easy JaCoCo Maven Plugin** simplifies JaCoCo code coverage setup in multi-module Maven projects by automatically gathering coverage data from all modules into a single, aggregated report. The plugin also offers project-wide coverage checks to enforce minimum coverage rules — all through a lean, minimal configuration effort.

This means you no longer need to copy-paste JaCoCo plugin configs or create special aggregator modules as [workarounds​](https://github.com/jacoco/jacoco/issues/869). Even the JaCoCo maintainers [envisioned that an external project could streamline Maven integration](https://github.com/jacoco/jacoco/issues/974#issuecomment-615225202) – Easy JaCoCo is that solution.


> **Note:** This plugin is activated via `.mvn/extensions.xml`, so there is **no need** to declare the plugin in your parent POM unless you wish to customize project checks.

## Why Easy JaCoCo?

Configuring JaCoCo in a multi-module Maven project can be complex and repetitive. Easy JaCoCo eliminates this hassle by:

- **Minimal Configuration:** Activate Easy JaCoCo in the `.mvn/extensions.xml` file – once enabled at the project level, it automatically instruments all modules and collects coverage data without any additional modifications to your modules’ POMs.
- **Project-wide Coverage Report:** It generates an **aggregated coverage report** (HTML, XML, CSV) for the entire project without the need for an extra aggregator module. All `.exec` files found in the project are merged automatically.
- **Project-wide Coverage Check:** Easily enforce overall coverage thresholds across all modules. For example, you can require that the project must have at least 70% line coverage. Configure your rules via the optional plugin configuration in your parent POM if needed.
- **Support for Both On-the-fly and Offline Instrumentation:** While Easy JaCoCo uses the on-the-fly JaCoCo agent by default, it also picks up offline instrumentation files. Any files ending with `.exec` anywhere in the project will be included in reports and checks by default.

## Quick Start

Easy JaCoCo is a Maven extension that requires only a single configuration file to activate it across your entire project.

### Step 1: Activate the Plugin

Create or update the **`.mvn/extensions.xml`** file in your project root and add:

```xml
<extensions>
  <extension>
    <groupId>com.marvinformatics.jacoco</groupId>
    <artifactId>easy-jacoco-maven-plugin</artifactId>
    <version>0.1</version>
  </extension>
</extensions>
```

### Step 2: (Optional) Customize Project Checks

If you want to enforce custom project-wide coverage rules (or add extra configuration), then declare the plugin in your parent POM. Otherwise, this step is not required. For example, to require at least 70% line coverage:

```xml
<!-- Parent or aggregator pom.xml -->
<build>
  <plugins>
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
  </plugins>
</build>
```

## Goals and Configuration

Easy JaCoCo comes with several Maven goals to generate reports or perform project-wide checks. In most builds these are bound automatically to the standard Maven phases. For advanced usage or troubleshooting, refer to the detailed documentation in the `/docs` directory:

- **[`check-project`](docs/check-project.md):** Aggregated coverage check that verifies your project meets coverage rules.
- **[`report-project`](docs/report-project.md):** Generates the aggregated project coverage report.
- **[`instrument-jar`](docs/instrument-jar.md):** Instruments jar files. In addition to on-the-fly instrumentation, the plugin can also pick up offline `.exec` files from anywhere in the project.
- **[`persist-report-project`](docs/persist-report-project.md):** Internal goal used by the lifecycle participant to persist generated report POMs.
- **[`help`](docs/help.md):** Shows help and parameters for all goals.

## Additional Notes

- **Skipping Coverage:** You can disable Easy JaCoCo by setting the property `-Deasyjacoco.skip=true` during your Maven build.
- **Integration with CI/SonarQube:** The aggregated XML report (`jacoco-aggregate/jacoco.xml`) can be fed directly into tools like SonarQube for unified coverage analysis.
- **How It Works:** Easy JaCoCo primarily uses the on-the-fly JaCoCo agent to collect coverage during test execution, but it will also include any offline `.exec` instrumentation files found in the project.
- **Requirements:** The plugin requires Maven 3.9.9+ and Java 17+.

For more detailed goal configuration and usage examples, please refer to the docs for each goal listed above.

---

Learn more and join the discussion: [Share Feedback & Ask Questions](https://chatgpt.com/share/67f927c9-7974-8003-b5f8-32f2e7e04ab7)

*This page was produced through a collaboration between human expertise and AI assistance.*

© 2025 [Marvinformatics](https://github.com/velo). Open-source under the Apache License 2.0.
