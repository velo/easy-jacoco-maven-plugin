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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

@Mojo(name = "instrument-jar", defaultPhase = LifecyclePhase.PACKAGE)
public class InstrumentJarMojo extends AbstractMojo {

  @Parameter(property = "easyjacoco.skip", defaultValue = "false")
  private boolean skip;

  @Parameter(property = "easyjacoco.source", required = true)
  private File source;

  @Parameter(property = "easyjacoco.destination", required = true)
  private File destination;

  @Override
  public void execute() throws MojoExecutionException {
    if (skip) {
      getLog().info("Instrumentation skipped via skip configuration");
      return;
    }

    getLog().info("Instrumenting file: " + source.getAbsolutePath());

    if (destination.exists()) {
      destination.delete();
    }
    var instrumenter = new Instrumenter(new OfflineInstrumentationAccessGenerator());

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
