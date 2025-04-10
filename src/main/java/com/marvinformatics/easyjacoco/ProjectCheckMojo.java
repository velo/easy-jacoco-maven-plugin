package com.marvinformatics.easyjacoco;

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
import org.apache.maven.project.MavenProject;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.check.IViolationsOutput;
import org.jacoco.report.check.Limit;
import org.jacoco.report.check.Rule;

import com.marvinformatics.easyjacoco.jacoco.FileFilter;
import com.marvinformatics.easyjacoco.jacoco.ReportSupport;

@Mojo(name = "check-project", defaultPhase = LifecyclePhase.VERIFY)
public class ProjectCheckMojo extends AbstractMojo implements IViolationsOutput {

    @Parameter(property = "easyjacoco.skip", defaultValue = "false")
    private boolean skip;

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession session;

    /**
	 * <p>
	 * Check configuration used to specify rules on element types (BUNDLE,
	 * PACKAGE, CLASS, SOURCEFILE or METHOD) with a list of limits. Each limit
	 * applies to a certain counter (INSTRUCTION, LINE, BRANCH, COMPLEXITY,
	 * METHOD, CLASS) and defines a minimum or maximum for the corresponding
	 * value (TOTALCOUNT, COVEREDCOUNT, MISSEDCOUNT, COVEREDRATIO, MISSEDRATIO).
	 * If a limit refers to a ratio it must be in the range from 0.0 to 1.0
	 * where the number of decimal places will also determine the precision in
	 * error messages. A limit ratio may optionally be declared as a percentage
	 * where 0.80 and 80% represent the same value.
	 * </p>
	 *
	 * <p>
	 * If not specified the following defaults are assumed:
	 * </p>
	 *
	 * <ul>
	 * <li>rule element: BUNDLE</li>
	 * <li>limit counter: INSTRUCTION</li>
	 * <li>limit value: COVEREDRATIO</li>
	 * </ul>
	 *
	 * <p>
	 * This example requires an overall instruction coverage of 80% and no class
	 * must be missed:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * <rules>
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
	 * </rules>}
	 * </pre>
	 *
	 * <p>
	 * This example requires a line coverage minimum of 50% for every class
	 * except test classes:
	 * </p>
	 *
	 * <pre>
	 * {@code
	 * <rules>
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
	 * </rules>}
	 * </pre>
	 */
	@Parameter(required = true)
	private List<RuleConfiguration> projectRules;

	/**
	 * Halt the build if any of the checks fail.
	 */
	@Parameter(property = "jacoco.haltOnFailure", defaultValue = "false", required = true)
	private boolean haltOnFailure;
	

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

	private boolean violations;
	
    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Project check skipped via skip configuration");
            return;
        }

        getLog().info("Running project wide check...");
        

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
			support.processProjects(visitor, session.getAllProjects(), includes, excludes);
			visitor.visitEnd();
		} catch (final IOException e) {
			throw new MojoExecutionException(
					"Error while checking code coverage: " + e.getMessage(), e);
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