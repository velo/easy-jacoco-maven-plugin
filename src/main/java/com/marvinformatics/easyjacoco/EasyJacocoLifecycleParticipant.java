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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.ModelWriter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("easy-jacoco")
@Singleton
public class EasyJacocoLifecycleParticipant extends AbstractMavenLifecycleParticipant {

  private static final Logger log = LoggerFactory.getLogger(EasyJacocoLifecycleParticipant.class);

  @Inject private ModelWriter modelWriter;
  @Inject private ProjectBuilder projectBuilder;

  @Override
  public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
    if ("true".equalsIgnoreCase(System.getProperty("easyjacoco.skip"))) {
      log.info("Skipping EasyJacoco lifecycle logic due to -Deasyjacoco.skip=true");
      return;
    }

    log.info("Registering jacoco related plugins on all modules");
    for (var project : session.getProjects()) {
      registerVanillaJacocoExecution(project);
    }

    MavenProject topLevelProject = session.getTopLevelProject();
    if (session.getProjects().size() > 1) {
      log.debug("Detected multi-module project, registering project report");
      var reportProject =
          newReportProject(
              topLevelProject, session.getProjects(), session.getProjectBuildingRequest());

      var allProjects = new ArrayList<>(session.getProjects());
      allProjects.add(reportProject);
      session.setProjects(allProjects);
    }
  }

  private MavenProject newReportProject(
      MavenProject topLevelProject,
      List<MavenProject> reactorProjects,
      ProjectBuildingRequest projectBuildingRequest)
      throws MavenExecutionException {
    var projectArtifactId =
        topLevelProject.getArtifactId().contains("-parent")
            ? topLevelProject.getArtifactId().replace("-parent", "-coverage")
            : topLevelProject.getArtifactId() + "-coverage";

    var outputDir = new File(topLevelProject.getBuild().getDirectory());
    var coverageProjectDir = new File(outputDir, projectArtifactId);
    var generatedPom = new File(coverageProjectDir, "pom.xml");

    if (!coverageProjectDir.exists() && !coverageProjectDir.mkdirs()) {
      throw new MavenExecutionException(
          "Failed to create output dir for coverage pom", generatedPom);
    }

    try {
      log.info("Generating report project: " + projectArtifactId);
      Model pom = new Model();
      pom.setModelVersion(topLevelProject.getModelVersion());
      Parent parent = new Parent();
      parent.setGroupId(topLevelProject.getGroupId());
      parent.setArtifactId(topLevelProject.getArtifactId());
      parent.setVersion(topLevelProject.getVersion());

      pom.setGroupId(topLevelProject.getGroupId());
      pom.setParent(parent);
      pom.setVersion(topLevelProject.getVersion());
      pom.setPackaging("pom");

      pom.setArtifactId(projectArtifactId);

      Properties extraProperties = readExtraProperties(topLevelProject);
      pom.setProperties(extraProperties);

      Build build = new Build();
      build.setOutputDirectory(topLevelProject.getBuild().getOutputDirectory());
      pom.setBuild(build);

      var pluginProps =
          readArtifactProperties("com.marvinformatics.jacoco", "easy-jacoco-maven-plugin");

      Plugin plugin = new Plugin();
      plugin.setGroupId("com.marvinformatics.jacoco");
      plugin.setArtifactId("easy-jacoco-maven-plugin");
      plugin.setVersion(pluginProps.getProperty("version"));

      var report = new PluginExecution();
      report.setId("report-project");
      report.setGoals(List.of("report-project"));

      var check = new PluginExecution();
      check.setId("check-project");
      check.setGoals(List.of("check-project"));

      var configuration = new Xpp3Dom("configuration");
      configuration.addChild(newPair("reportGroupId", pom.getGroupId()));
      configuration.addChild(newPair("reportArtifactId", pom.getArtifactId()));
      configuration.addChild(newPair("reportVersion", pom.getVersion()));

      var persist = new PluginExecution();
      persist.setId("persist-report-project");
      persist.setGoals(List.of("persist-report-project"));
      persist.setConfiguration(configuration);
      plugin.setExecutions(List.of(report, check, persist));

      build.setPlugins(List.of(plugin));

      List<Dependency> dependencies =
          reactorProjects.stream()
              .filter(reactorProject -> !reactorProject.equals(topLevelProject))
              .map(dep -> toDependency(dep))
              .collect(Collectors.toList());
      pom.setDependencies(dependencies);

      modelWriter.write(generatedPom, null, pom);

      return projectBuilder.build(generatedPom, projectBuildingRequest).getProject();

    } catch (Exception e) {
      throw new MavenExecutionException("Failed to generate coverage pom.", e);
    }
  }

  private Properties readExtraProperties(MavenProject topLevelProject) {
    var plugin = topLevelProject.getPlugin("com.marvinformatics.jacoco:easy-jacoco-maven-plugin");
    if (plugin == null) {
      return new Properties();
    }

    if (!(plugin.getConfiguration() instanceof Xpp3Dom)) {
      return new Properties();
    }

    var config = (Xpp3Dom) plugin.getConfiguration();
    var projectExtraProperties = config.getChild("projectExtraProperties");
    if (projectExtraProperties == null || projectExtraProperties.getChildCount() == 0) {
      return new Properties();
    }

    var properties = new Properties();
    for (Xpp3Dom child : projectExtraProperties.getChildren()) {
      properties.setProperty(child.getName(), child.getValue());
    }

    return properties;
  }

  private Dependency toDependency(MavenProject project) {
    Dependency dependency = new Dependency();
    dependency.setGroupId(project.getGroupId());
    dependency.setArtifactId(project.getArtifactId());
    dependency.setVersion(project.getVersion());
    dependency.setType(project.getPackaging());
    return dependency;
  }

  private Plugin registerVanillaJacocoExecution(MavenProject project)
      throws MavenExecutionException {
    var jacocoPomProps = readArtifactProperties("org.jacoco", "org.jacoco.core");

    var plugin = project.getPlugin("org.jacoco:jacoco-maven-plugin");
    if (plugin == null) {
      plugin = new Plugin();
      plugin.setGroupId("org.jacoco");
      plugin.setArtifactId("jacoco-maven-plugin");
      plugin.setVersion(jacocoPomProps.getProperty("version"));
      project.getBuild().addPlugin(plugin);
    }

    var execution = new PluginExecution();
    execution.setId("vanilla-jacoco-goals");
    execution.setGoals(
        List.of("prepare-agent", "prepare-agent-integration", "report", "report-integration"));
    plugin.setExecutions(Collections.singletonList(execution));

    return plugin;
  }

  private Xpp3Dom newPair(String name, String value) {
    var dom = new Xpp3Dom(name);
    dom.setValue(value);
    return dom;
  }

  public static Properties readArtifactProperties(String groupId, String artifactId)
      throws MavenExecutionException {
    var resource = "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
    try (var stream = EasyJacocoLifecycleParticipant.class.getResourceAsStream(resource)) {
      if (stream == null) {
        throw new MavenExecutionException(
            "Properties not found for " + groupId + ":" + artifactId, new IOException());
      }
      var props = new Properties();
      props.load(stream);
      return props;
    } catch (IOException e) {
      throw new MavenExecutionException(
          "Unable to read properties for " + groupId + ":" + artifactId, e);
    }
  }
}
