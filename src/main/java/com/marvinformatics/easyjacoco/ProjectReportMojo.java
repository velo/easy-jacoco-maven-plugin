package com.marvinformatics.easyjacoco;

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

import com.marvinformatics.easyjacoco.jacoco.FileFilter;
import com.marvinformatics.easyjacoco.jacoco.ReportFormat;
import com.marvinformatics.easyjacoco.jacoco.ReportSupport;

/**
 * Generates an aggregated project report (placeholder).
 */
@Mojo(name = "report-project", defaultPhase = LifecyclePhase.VERIFY)
public class ProjectReportMojo extends AbstractMojo {

	/**
	 * Skip execution of the mojo. Can be set via -Deasyjacoco.skip=true
	 */
	@Parameter(property = "easyjacoco.skip", defaultValue = "false")
	private boolean skip;

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession session;

	/**
	 * Output directory for the reports. Note that this parameter is only relevant
	 * if the goal is run from the command line or from the default build lifecycle.
	 * If the goal is run indirectly as part of a site generation, the output
	 * directory configured in the Maven Site Plugin is used instead.
	 */
	@Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco-aggregate")
	File outputDirectory;

	/**
	 * Encoding of the generated reports.
	 */
	@Parameter(property = "project.reporting.outputEncoding", defaultValue = "UTF-8")
	String outputEncoding;

	/**
	 * A list of report formats to generate. Supported formats are HTML, XML and
	 * CSV. Defaults to all formats if no values are given.
	 *
	 * @since 0.8.7
	 */
	@Parameter(defaultValue = "HTML,XML,CSV")
	List<ReportFormat> formats;

	/**
	 * Name of the root node HTML report pages.
	 *
	 * @since 0.7.7
	 */
	@Parameter(defaultValue = "${project.name}")
	String title;

	/**
	 * Footer text used in HTML report pages.
	 *
	 * @since 0.7.7
	 */
	@Parameter
	String footer;

	/**
	 * Encoding of the source files.
	 */
	@Parameter(property = "project.build.sourceEncoding", defaultValue = "UTF-8")
	String sourceEncoding;

	/**
	 * A list of class files to include in the report. May use wildcard characters
	 * (* and ?). When not specified everything will be included.
	 */
	@Parameter
	List<String> includes;

	/**
	 * A list of class files to exclude from the report. May use wildcard characters
	 * (* and ?). When not specified nothing will be excluded.
	 */
	@Parameter
	List<String> excludes;

	/**
	 * A list of execution data files to include in the report from each project.
	 * May use wildcard characters (* and ?). When not specified all *.exec files
	 * from the target folder will be included.
	 */
	@Parameter
	List<String> dataFileIncludes;

	/**
	 * A list of execution data files to exclude from the report. May use wildcard
	 * characters (* and ?). When not specified nothing will be excluded.
	 */
	@Parameter
	List<String> dataFileExcludes;

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
				support.addVisitor(f.createVisitor(outputDirectory, outputEncoding, Locale.getDefault(), footer));
			}

			final IReportVisitor visitor = support.initRootVisitor();
			createReport(visitor, support);
			visitor.visitEnd();
		} catch (final IOException e) {
			throw new MojoExecutionException("Error while creating report: " + e.getMessage(), e);
		}
	}

	void createReport(final IReportGroupVisitor visitor, final ReportSupport support) throws IOException {
		final IReportGroupVisitor group = visitor.visitGroup(title);

		for (MavenProject project : session.getAllProjects()) {
			if(project.getPackaging().equals("pom")) {
				continue;
			}
			support.processProject(group, project.getArtifactId(), project, includes, excludes, sourceEncoding);
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
