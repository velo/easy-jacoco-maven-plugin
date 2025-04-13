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
/* File: src/test/java/com/marvinformatics/easyjacoco/ExampleProjectMavenBuildTest.java */
package com.marvinformatics.easyjacoco;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.PrintStreamHandler;
import org.junit.jupiter.api.Test;

public class ExampleProjectMavenBuildIT {

  @Test
  void givenExampleProject_whenMavenCleanInstall_thenBuildSuccess() throws Exception {
    // Locate the example project directory.
    File projectDir = new File("target/test-classes/unit/example");
    assertThat(projectDir)
        .as("Example project directory should exist and be a directory")
        .exists()
        .isDirectory();

    // Set up the Maven invocation request.
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(new File(projectDir, "pom.xml"));
    request.setGoals(Collections.singletonList("clean install"));
    request.setBatchMode(true);
    request.setJavaHome(new File(System.getProperty("java.home")));
    request.setShowErrors(true);

    // Prepare the Maven invoker.
    Invoker invoker = new DefaultInvoker();
    File mavenHome = MavenDownloader.downloadAndExtractMaven("3.9.9");
    invoker.setMavenHome(mavenHome);

    // Capture Maven output.
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    invoker.setOutputHandler(new PrintStreamHandler(new PrintStream(outputStream), true));

    // Execute the Maven build.
    InvocationResult result = invoker.execute(request);

    String buildOutput = outputStream.toString();
    System.out.println(buildOutput); // useful for debugging in the IDE

    // Use AssertJ to assert that the build was successful.
    assertThat(result.getExitCode())
        .as(
            "Maven build should succeed (exit code 0) but got %s. Build output:%n%s",
            result.getExitCode(), buildOutput)
        .isEqualTo(0);

    assertThat(buildOutput)
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
}
