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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.junit.jupiter.api.Test;

public class ExampleProjectMavenBuildIT {

  @Test
  void givenExampleProject_whenMavenCleanInstallWithJacoco_thenBuildSuccess() throws Exception {
    // Execute the Maven build.
    TestResult result = runExample("examples/basic", "3.9.9");

    System.out.println(result.buildOutput); // useful for debugging in the IDE

    // Use AssertJ to assert that the build was successful.
    assertThat(result.exitCode)
        .as(
            "Maven build should succeed (exit code 0) but got %s. Build output:%n%s",
            result.exitCode, result.buildOutput)
        .isEqualTo(0);

    assertThat(result.buildOutput)
        // verify coverage pom.xml was written to disk
        .containsSubsequence(
            "persist-report-project (persist-report-project) @ sample-coverage",
            "Persisted generated project")
        // verify ProjectReportMojo was executed
        .containsSubsequence(
            "report-project (report-project) @ sample-coverage",
            "Running project aggregation report",
            "Project report available at:")
        // verify ProjectCheckMojo was executed
        .containsSubsequence(
            "check-project (check-project) @ sample-coverage",
            "Rule violated for bundle project",
            "Coverage checks have not been met");
  }

  private TestResult runExample(String example, String mavenVersion, String... args)
      throws IOException, MavenInvocationException, MavenExecutionException {
    // Locate the example project source directory.
    File srcProjectDir = new File(example);
    assertThat(srcProjectDir)
        .as("Example project directory should exist and be a directory")
        .exists()
        .isDirectory();

    // Copy the example project to a temporary directory under "target/"
    File targetDir = new File("target/testing", "example-temp-" + System.currentTimeMillis());
    copyDirectory(srcProjectDir.toPath(), targetDir.toPath());

    // Use the temporary directory as the project directory for the Maven build.
    File projectDir = targetDir;

    String jacocoVersion =
        EasyJacocoLifecycleParticipant.readArtifactProperties("org.jacoco", "org.jacoco.agent.rt")
            .getProperty("version");

    if (args == null || args.length == 0 || (args.length == 1 && args[0] == null)) {
      String easyJacocoVersion =
          EasyJacocoLifecycleParticipant.readArtifactProperties(
                  "com.marvinformatics.jacoco", "easy-jacoco-maven-plugin")
              .getProperty("version");
      args =
          new String[] {
            "clean",
            "install",
            "-Deasy-jacoco.version=" + easyJacocoVersion,
            "-Djacoco.version=" + jacocoVersion
          };
    }

    // Set up the Maven invocation request.
    InvocationRequest request =
        new DefaultInvocationRequest()
            .setPomFile(new File(projectDir, "pom.xml"))
            .setShellEnvironmentInherited(false)
            .addArgs(Lists.newArrayList(args))
            .setBatchMode(true)
            .setJavaHome(new File(System.getProperty("java.home")))
            .setShowErrors(true)
            .setDebug(true);

    // Inject the Jacoco agent into Maven's JVM.
    // Determine the jacoco agent jar path. Adjust version if necessary.
    String jacocoAgentPath =
        System.getProperty("user.home")
            + "/.m2/repository/org/jacoco/org.jacoco.agent/"
            + jacocoVersion
            + "/org.jacoco.agent-"
            + jacocoVersion
            + "-runtime.jar";
    // Set the destination file for the coverage report under the temporary project directory.
    File jacocoDest = new File(projectDir, "target/jacoco.exec");
    // Compose the JAVA_OPTS argument that injects the agent.
    String mavenOpts =
        "-javaagent:" + jacocoAgentPath + "=destfile=" + jacocoDest.getAbsolutePath();
    request.setMavenOpts(mavenOpts);

    // Prepare the Maven invoker.
    Invoker invoker = new DefaultInvoker();
    File mavenHome = MavenDownloader.downloadAndExtractMaven(mavenVersion);
    invoker.setMavenHome(mavenHome);

    // Capture Maven output.
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    invoker.setOutputHandler(new PrintStreamHandler(new PrintStream(outputStream), true));

    // Execute the Maven build.
    InvocationResult invocationResult = invoker.execute(request);
    String buildOutput = outputStream.toString();

    assertThat(jacocoDest)
        .as("Maven execution terminated without generating jacoco report")
        .exists();

    return new TestResult(
        buildOutput, invocationResult.getExecutionException(), invocationResult.getExitCode());
  }

  /** Recursively copies a directory. */
  private static void copyDirectory(Path source, Path target) throws IOException {
    Files.walkFileTree(
        source,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws IOException {
            Path targetDir = target.resolve(source.relativize(dir));
            Files.createDirectories(targetDir);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            Path targetFile = target.resolve(source.relativize(file));
            Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
            return FileVisitResult.CONTINUE;
          }
        });
  }

  static class TestResult {

    final String buildOutput;
    final CommandLineException executionException;
    final int exitCode;

    public TestResult(String buildOutput, CommandLineException executionException, int exitCode) {
      this.buildOutput = buildOutput;
      this.executionException = executionException;
      this.exitCode = exitCode;
    }
  }
}
