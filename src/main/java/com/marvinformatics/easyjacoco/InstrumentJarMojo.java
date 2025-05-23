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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.jacoco.core.runtime.WildcardMatcher;

/**
 * Maven Mojo for instrumenting jar files by injecting JaCoCo probe instructions to enable offline
 * code coverage analysis.
 *
 * <p>This goal, named "instrument-jar", is executed during the package phase. It reads a specified
 * source file (or jar), applies offline instrumentation using JaCoCo, and writes the resulting
 * instrumented file to the provided destination.
 */
@Mojo(name = "instrument-jar", defaultPhase = LifecyclePhase.PACKAGE)
public class InstrumentJarMojo extends AbstractMojo {
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

  /** When set to true, instrumentation will be skipped. */
  @Parameter(property = "easyjacoco.skip", defaultValue = "false")
  private boolean skip;

  /** The source file (or JAR) that will be instrumented. */
  @Parameter(property = "easyjacoco.source", required = true)
  private File source;

  /** The target file for the instrumented output. */
  @Parameter(property = "easyjacoco.destination", required = true)
  private File destination;

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("Instrumentation skipped via skip configuration");
      return;
    }

    getLog().info("Instrumenting file: " + source.getAbsolutePath());

    if (source.equals(destination)) {
      source = new File(source.getParentFile(), source.getName() + ".original");
      getLog().info("In place instrumentation, renamed original file to: " + source);
      destination.renameTo(source);
    }

    if (destination.exists()) {
      destination.delete();
    }

    if (includes == null) {
      includes = List.of("**/*.class");
    }
    if (excludes == null) {
      excludes = List.of();
    }

    WildcardMatcher includes =
        new WildcardMatcher(this.includes.stream().collect(Collectors.joining(":")));
    WildcardMatcher excludes =
        new WildcardMatcher(this.excludes.stream().collect(Collectors.joining(":")));

    var instrumenter =
        new Instrumenter(new OfflineInstrumentationAccessGenerator()) {
          public byte[] instrument(final byte[] buffer, final String name) throws IOException {
            var classname = name.split("@")[1];
            if (includes.matches(classname) && !excludes.matches(classname)) {
              getLog().debug(String.format("Instrumenting class %s", classname));
              return super.instrument(buffer, classname);
            }
            getLog().debug(String.format("Skip instrumentation for %s", classname));
            return buffer;
          }
        };

    destination.getParentFile().mkdirs();
    try (final InputStream input = new FileInputStream(source);
        final OutputStream output = new FileOutputStream(destination)) {
      instrumenter.instrumentAll(input, output, source.getAbsolutePath());
      getLog().info("Instrumented jar saved to: " + destination.getAbsolutePath());
    } catch (final IOException e) {
      destination.delete();
      throw new MojoExecutionException(e);
    }
  }
}
