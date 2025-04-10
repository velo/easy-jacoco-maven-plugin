package com.marvinformatics.easyjacoco;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.LifecycleModuleBuilder;
import org.apache.maven.lifecycle.internal.LifecycleTaskSegmentCalculator;
import org.apache.maven.model.io.ModelWriter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.ProjectBuilder;
/**
 * Generates an aggregated project report (placeholder).
 */
@Mojo(name = "persist-report-project", aggregator = true, defaultPhase = LifecyclePhase.VALIDATE)
public class PersistProjectForReportMojo extends AbstractMojo {

    /**
     * Skip execution of the mojo.
     * Can be set via -Deasyjacoco.skip=true
     */
    @Parameter(property = "easyjacoco.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;
    
    @Parameter( property = "easyjacoco.reportGroupId")
    private String reportGroupId;
    @Parameter(property = "easyjacoco.reportArtifactId")
    private String reportArtifactId;
    @Parameter(property = "easyjacoco.reportVersion")
    private String reportVersion;
    
    @Parameter(property = "easyjacoco.report.name")
    private String projectName;
    @Parameter(property = "easyjacoco.report.description")
    private String projectDescription;
    
    @Component
    private LifecycleTaskSegmentCalculator segmentCalculator;
    @Component
    private LifecycleModuleBuilder builder;
    @Component
    private ModelWriter modelWriter;
    @Component
    private ProjectBuilder projectBuilder;
    

    @Parameter(defaultValue = "${maven.version}", readonly = true)
    private String mavenVersion;
    
    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Easy jacoco skipped via skip configuration");
            return;
        }

        var projectOpt = session.getProjects().stream().filter(p -> reportGroupId.equals(p.getGroupId() )&& reportArtifactId.equals(p.getArtifactId()) &&reportVersion.equals(p.getVersion() )).findFirst();

        if(projectOpt.isEmpty()) {
        	throw new MojoExecutionException("Module not found %s:%s:%s".formatted(reportGroupId, reportArtifactId, reportVersion));
        }
        
        var project = projectOpt.get();

        if(project.getFile().exists()) {
        	return;
        }
        
        project.getFile().getParentFile().mkdirs();
        
        var model = project.getModel();
        if(isNotEmpty(projectName)) {
        	model.setName(projectName);
        }
        if(isNotEmpty(projectDescription)) {
        	model.setDescription(projectDescription);
        }
        
        try {
			modelWriter.write(project.getFile(), null, model);
		} catch (IOException e) {
			throw new MojoExecutionException(e);
		}
        
        getLog().info("Persisted generated project " + project.getFile());
    }
    

	private boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}
	
	public static boolean isEmpty( Object obj) {
		if (obj == null) {
			return true;
		}

		if (obj instanceof Optional<?> optional) {
			return optional.isEmpty();
		}
		if (obj instanceof CharSequence charSequence) {
			return charSequence.isEmpty();
		}
		if (obj.getClass().isArray()) {
			return Array.getLength(obj) == 0;
		}
		if (obj instanceof Collection<?> collection) {
			return collection.isEmpty();
		}
		if (obj instanceof Map<?, ?> map) {
			return map.isEmpty();
		}

		// else
		return false;
	}
     
}
