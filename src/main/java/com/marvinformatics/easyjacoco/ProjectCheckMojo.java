/*
 * Copyright © ${year} DataSQRL (contact@datasqrl.com)
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

  @Parameter(property = "easyjacoco.skip", defaultValue = "false")
  private boolean skip;

  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;

  /**
   * Check configuration used to specify rules on element types (BUNDLE, PACKAGE, CLASS, SOURCEFILE
   * or METHOD) with a list of limits. Each limit applies to a certain counter (INSTRUCTION, LINE,
   * BRANCH, COMPLEXITY, METHOD, CLASS) and defines a minimum or maximum for the corresponding value
   * (TOTALCOUNT, COVEREDCOUNT, MISSEDCOUNT, COVEREDRATIO, MISSEDRATIO). If a limit refers to a
   * ratio it must be in the range from 0.0 to 1.0 where the number of decimal places will also
   * determine the precision in error messages. A limit ratio may optionally be declared as a
   * percentage where 0.80 and 80% represent the same value.
   *
   * <p>If not specified the following defaults are assumed:
   *
   * <ul>
   *   <li>rule element: BUNDLE
   *   <li>limit counter: INSTRUCTION
   *   <li>limit value: COVEREDRATIO
   * </ul>
   *
   * <p>This example requires an overall instruction coverage of 80% and no class must be missed:
   *
   * <pre>{@code
   * <projectRules>
   *   <rule>
   *     <element>BUNDLE</element>
   *     <limits>
   *       <limit>
   *         <counter>INSTRUCTION</counter>
   *         <value>COVEREDRATIO</value>
   *         <minimum>0.80</minimum>
   *       </limit>
   *       <limit>
   *         <counter>CLASS</counter>
   *         <value>MISSEDCOUNT</value>
   *         <maximum>0</maximum>
   *       </limit>
   *     </limits>
   *   </rule>
   * </projectRules>
   * }</pre>
   *
   * If undefined, the value above is used as default
   *
   * <p>This example requires a line coverage minimum of 50% for every class except test classes:
   *
   * <pre>{@code
   * <projectRules>
   *   <rule>
   *     <element>CLASS</element>
   *     <excludes>
   *       <exclude>*Test</exclude>
   *     </excludes>
   *     <limits>
   *       <limit>
   *         <counter>LINE</counter>
   *         <value>COVEREDRATIO</value>
   *         <minimum>50%</minimum>
   *       </limit>
   *     </limits>
   *   </rule>
   * </projectRules>
   * }</pre>
   */
  @Parameter private List<RuleConfiguration> projectRules;

  /** Halt the build if any of the checks fail. */
  @Parameter(property = "jacoco.haltOnFailure", defaultValue = "false", required = true)
  private boolean haltOnFailure;

  /**
   * A list of class files to include in coverage check. May use wildcard characters (* and ?). When
   * not specified everything will be included.
   */
  @Parameter List<String> includes;

  /**
   * A list of class files to exclude from coverage check. May use wildcard characters (* and ?).
   * When not specified nothing will be excluded.
   */
  @Parameter List<String> excludes;

  /**
   * A list of execution data files to include in coverage check from each project. May use wildcard
   * characters (* and ?). When not specified all *.exec files from the target folder will be
   * included.
   */
  @Parameter List<String> dataFileIncludes;

  /**
   * A list of execution data files to exclude from coverage check. May use wildcard characters (*
   * and ?). When not specified nothing will be excluded.
   */
  @Parameter List<String> dataFileExcludes;

  /**
   * A list of modules/projects to exclude from coverage check. Must match the module artifactId.
   * When not specified nothing will be excluded.
   */
  @Parameter List<String> excludeModules;

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
    for (final File execFile : filter.getFiles(projectRoot)) {
      support.loadExecutionData(execFile);
    }
  }

  @Override
  public void onViolation(ICoverageNode node, Rule rule, Limit limit, String message) {
    this.getLog().warn(message);
    violations = true;
  }
}
