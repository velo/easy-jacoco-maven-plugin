/*
 * Copyright © 2025 Marvin Froeder (contact@marvinformatics.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marvinformatics.easyjacoco;

import com.marvinformatics.easyjacoco.jacoco.FileFilter;
import com.marvinformatics.easyjacoco.jacoco.ReportSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jacoco.core.analysis.ICounter.CounterValue;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.check.IViolationsOutput;
import org.jacoco.report.check.Limit;
import org.jacoco.report.check.Rule;

@Mojo(name = "check-project", defaultPhase = LifecyclePhase.VERIFY)
public class ProjectCheckMojo extends AbstractMojo implements IViolationsOutput {

  /**
   * Skip the execution of the check-project goal.
   *
   * <p>Default: false.
   */
  @Parameter(property = "easyjacoco.skip", defaultValue = "false")
  private boolean skip;

  /** Maven session object provided by the Maven runtime. */
  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;

  /**
   * Check configuration used to specify rules on element types (BUNDLE, PACKAGE, CLASS, SOURCEFILE
   * or METHOD) along with limits. Each limit applies to a specific counter (INSTRUCTION, LINE,
   * BRANCH, COMPLEXITY, METHOD, CLASS) and defines a minimum or maximum for the corresponding
   * metric (TOTALCOUNT, COVEREDCOUNT, MISSEDCOUNT, COVEREDRATIO, MISSEDRATIO).
   *
   * <p>If not specified, JaCoCo defaults are used:
   *
   * <ul>
   *   <li>Element: BUNDLE
   *   <li>Counter: INSTRUCTION with minimum COVEREDRATIO of 0.80 and CLASS with maximum MISSEDCOUNT
   *       of 0
   * </ul>
   *
   * <p>For example, to enforce a minimum of 70% line coverage for the entire project (bundle),
   * configure:
   *
   * <pre>{@code
   * <projectRules>
   *   <rule>
   *     <element>BUNDLE</element>
   *     <limits>
   *       <limit>
   *         <counter>LINE</counter>
   *         <value>COVEREDRATIO</value>
   *         <minimum>0.70</minimum>
   *       </limit>
   *     </limits>
   *   </rule>
   * </projectRules>
   * }</pre>
   *
   * @since 0.1
   */
  @Parameter private List<RuleConfiguration> projectRules;

  /**
   * Whether to halt the build if the coverage check fails.
   *
   * <p>Default: false (warn only).
   */
  @Parameter(property = "jacoco.haltOnFailure", defaultValue = "false", required = true)
  private boolean haltOnFailure;

  /**
   * A list of class files to include in coverage check. May use wildcard characters (* and ?). When
   * not specified everything will be included.
   */
  @Parameter private List<String> includes;

  /**
   * List of class files to exclude from the coverage check. Supports wildcards.
   *
   * <p>Default: None.
   */
  @Parameter private List<String> excludes;

  /**
   * List of execution data files to include for coverage analysis. Supports wildcards.
   *
   * <p>Default: All *.exec files in target directories.
   */
  @Parameter private List<String> dataFileIncludes;

  /**
   * List of execution data files to exclude from coverage analysis. Supports wildcards.
   *
   * <p>Default: None.
   */
  @Parameter private List<String> dataFileExcludes;

  /**
   * List of module artifactIds to exclude from the coverage check.
   *
   * <p>Default: None.
   */
  @Parameter private List<String> excludeModules;

  private boolean violations;

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("Project check skipped via skip configuration");
      return;
    }

    getLog().info("Running project wide check...");

    if (projectRules == null || projectRules.isEmpty()) {
      // set default value to rules as per javadoc
      projectRules = new ArrayList<RuleConfiguration>();
      RuleConfiguration rule = new RuleConfiguration();
      rule.setElement("BUNDLE");
      List<Limit> limits = new ArrayList<Limit>();
      rule.setLimits(limits);
      Limit instructions = new Limit();
      instructions.setCounter(CounterEntity.INSTRUCTION.name());
      instructions.setValue(CounterValue.COVEREDRATIO.name());
      instructions.setMinimum("0.80");
      limits.add(instructions);
      Limit classes = new Limit();
      classes.setCounter(CounterEntity.CLASS.name());
      classes.setValue(CounterValue.MISSEDCOUNT.name());
      classes.setMaximum("0");
      limits.add(classes);
      projectRules.add(rule);
    }

    var projectRoot = session.getTopLevelProject().getBasedir();

    final ReportSupport support = new ReportSupport(getLog());

    final List<Rule> checkerrules = new ArrayList<Rule>();
    for (final RuleConfiguration r : projectRules) {
      checkerrules.add(r.rule);
    }
    support.addRulesChecker(checkerrules, this);

    try {
      final IReportVisitor visitor = support.initRootVisitor();
      loadExecutionData(support, projectRoot);
      support.processProjects(
          visitor, session.getAllProjects(), includes, excludes, excludeModules);
      visitor.visitEnd();
    } catch (final IOException e) {
      throw new MojoExecutionException("Error while checking code coverage: " + e.getMessage(), e);
    }
    if (violations) {
      if (this.haltOnFailure) {
        throw new MojoExecutionException("Coverage checks have not been met. See log for details.");
      } else {
        this.getLog().warn("Coverage checks have not been met. See log for details.");
      }
    } else {
      this.getLog().info("All coverage checks have been met.");
    }
  }

  void loadExecutionData(final ReportSupport support, File projectRoot) throws IOException {
    if (dataFileIncludes == null) {
      dataFileIncludes = List.of("**/target/*.exec");
    }

    final FileFilter filter = new FileFilter(dataFileIncludes, dataFileExcludes);
    List<File> files = filter.getFiles(projectRoot);

    if (files.isEmpty()) {
      getLog()
          .error(
              String.format(
                  "No execution data found at: %s includes: %s excludes: %s",
                  projectRoot, dataFileIncludes, dataFileExcludes));
    }

    for (final File execFile : files) {
      support.loadExecutionData(execFile);
    }
  }

  @Override
  public void onViolation(ICoverageNode node, Rule rule, Limit limit, String message) {
    this.getLog().warn(message);
    violations = true;
  }
}
