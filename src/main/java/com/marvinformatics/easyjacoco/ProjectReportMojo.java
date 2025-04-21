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
import com.marvinformatics.easyjacoco.jacoco.ReportFormat;
import com.marvinformatics.easyjacoco.jacoco.ReportSupport;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;

/**
 * Generates an aggregated coverage report for the entire project by merging execution data from all
 * modules.
 *
 * <p>This goal is automatically bound to the verify phase and produces a report in multiple formats
 * (HTML, XML, CSV) placed in the directory defined by <code>
 * ${project.reporting.outputDirectory}/jacoco-aggregate</code>. It works similarly to the JaCoCo
 * report-aggregate mojo, but without requiring a separate aggregator project or manual dependency
 * management.
 */
@Mojo(name = "report-project", defaultPhase = LifecyclePhase.VERIFY)
public class ProjectReportMojo extends AbstractMojo {

  /** Skip execution of the mojo. Can be set via -Deasyjacoco.skip=true */
  @Parameter(property = "easyjacoco.skip", defaultValue = "false")
  private boolean skip;

  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;

  /**
   * Output directory for the reports. Note that this parameter is only relevant if the goal is run
   * from the command line or from the default build lifecycle. If the goal is run indirectly as
   * part of a site generation, the output directory configured in the Maven Site Plugin is used
   * instead.
   */
  @Parameter(defaultValue = "${project.build.directory}/jacoco-project-report")
  private File outputDirectory;

  /** Encoding of the generated reports. */
  @Parameter(property = "project.reporting.outputEncoding", defaultValue = "UTF-8")
  private String outputEncoding;

  /**
   * A list of report formats to generate. Supported formats are HTML, XML and CSV. Defaults to all
   * formats if no values are given.
   *
   * @since 0.8.7
   */
  @Parameter(defaultValue = "HTML,XML,CSV")
  private List<ReportFormat> formats;

  /**
   * Name of the root node HTML report pages.
   *
   * @since 0.7.7
   */
  @Parameter(defaultValue = "${project.name}")
  private String title;

  /**
   * Footer text used in HTML report pages.
   *
   * @since 0.7.7
   */
  @Parameter String footer;

  /** Encoding of the source files. */
  @Parameter(property = "project.build.sourceEncoding", defaultValue = "UTF-8")
  private String sourceEncoding;

  /**
   * A list of class files to include in the report. May use wildcard characters (* and ?). When not
   * specified everything will be included.
   */
  @Parameter private List<String> includes;

  /**
   * A list of class files to exclude from the report. May use wildcard characters (* and ?). When
   * not specified nothing will be excluded.
   */
  @Parameter private List<String> excludes;

  /**
   * A list of execution data files to include in the report from each project. May use wildcard
   * characters (* and ?). When not specified all *.exec files from the target folder will be
   * included.
   */
  @Parameter private List<String> dataFileIncludes;

  /**
   * A list of execution data files to exclude from the report. May use wildcard characters (* and
   * ?). When not specified nothing will be excluded.
   */
  @Parameter private List<String> dataFileExcludes;

  /**
   * A list of modules/projects to exclude from the report. Must match the module artifactId. When
   * not specified nothing will be excluded.
   */
  @Parameter private List<String> excludeModules;

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("Project report aggregation skipped via skip configuration");
      return;
    }

    getLog().info("Running project aggregation report...");
    // Future logic here

    var projectRoot = session.getTopLevelProject().getBasedir();

    try {
      final ReportSupport support = new ReportSupport(getLog());
      loadExecutionData(support, projectRoot);
      outputDirectory.mkdirs();

      for (final ReportFormat f : formats) {
        support.addVisitor(
            f.createVisitor(outputDirectory, outputEncoding, Locale.getDefault(), footer));
      }

      final IReportVisitor visitor = support.initRootVisitor();
      createReport(visitor, support);
      visitor.visitEnd();

      getLog().info(String.format("Project report available at: %s", outputDirectory));
    } catch (final IOException e) {
      throw new MojoExecutionException("Error while creating report: " + e.getMessage(), e);
    }
  }

  void createReport(final IReportGroupVisitor visitor, final ReportSupport support)
      throws IOException {
    final IReportGroupVisitor group = visitor.visitGroup(title);

    for (MavenProject project : session.getAllProjects()) {
      if (project.getPackaging().equals("pom")) {
        continue;
      }
      support.processProject(
          group,
          project.getArtifactId(),
          project,
          includes,
          excludes,
          sourceEncoding,
          excludeModules);
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
}
