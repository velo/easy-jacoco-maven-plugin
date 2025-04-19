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
import java.net.URL;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class MavenDownloader {

  /**
   * Downloads and extracts the specified Apache Maven distribution as a tar.gz file if it is not
   * already present in the Maven wrapper caches (i.e. ~/.m2/invoker). It checks for either
   * "apache-maven-[version]-bin" or "apache-maven-[version]". If found, returns the directory.
   *
   * @param mavenVersion the Maven version (e.g., "3.9.9")
   * @return a File representing the Maven home directory
   * @throws IOException if downloading or extraction fails
   */
  public static File downloadAndExtractMaven(String mavenVersion) throws IOException {
    // Locate the Maven wrapper distributions directory: ~/.m2/invoker
    String userHome = System.getProperty("user.home");
    File wrapperDists = new File(userHome, ".m2/invoker");
    if (!wrapperDists.exists() && !wrapperDists.mkdirs()) {
      throw new IOException(
          "Failed to create Maven wrapper directory at " + wrapperDists.getAbsolutePath());
    }

    // Try common directory names: with "-bin" first, then without.
    File mavenDir = new File(wrapperDists, "apache-maven-" + mavenVersion + "-bin");
    if (!mavenDir.exists()) {
      mavenDir = new File(wrapperDists, "apache-maven-" + mavenVersion);
    }
    if (mavenDir.exists()) {
      return mavenDir;
    }

    // If the distribution is not found, download it from the Apache archive as a tar.gz
    String mavenDownloadUrl =
        String.format(
            "https://archive.apache.org/dist/maven/maven-3/%s/binaries/apache-maven-%s-bin.tar.gz",
            mavenVersion, mavenVersion);

    // Download the tar.gz file into the Maven wrapper directory.
    File tarGzFile = new File(wrapperDists, "apache-maven-" + mavenVersion + "-bin.tar.gz");
    if (!tarGzFile.exists()) {
      try (InputStream in = new URL(mavenDownloadUrl).openStream();
          FileOutputStream out = new FileOutputStream(tarGzFile)) {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
          out.write(buffer, 0, len);
        }
      }
    }

    // Extract the tar.gz file contents into the Maven wrapper directory.
    untarGz(tarGzFile, wrapperDists);

    // After extraction, the directory should now exist.
    mavenDir = new File(wrapperDists, "apache-maven-" + mavenVersion + "-bin");
    if (!mavenDir.exists()) {
      mavenDir = new File(wrapperDists, "apache-maven-" + mavenVersion);
    }
    if (!mavenDir.exists()) {
      throw new IOException(
          "Maven extraction failed: expected directory not found: " + mavenDir.getAbsolutePath());
    }
    return mavenDir;
  }

  /**
   * Extracts the given tar.gz file into the specified target directory using Apache Commons
   * Compress. Sets executable flag for files when the tar entry's mode indicates execute
   * permission.
   *
   * @param tarGzFile the tar.gz file to extract
   * @param targetDir the directory to extract into
   * @throws IOException if an I/O error occurs during extraction
   */
  public static void untarGz(File tarGzFile, File targetDir) throws IOException {
    try (FileInputStream fis = new FileInputStream(tarGzFile);
        GZIPInputStream gis = new GZIPInputStream(fis);
        TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {
      TarArchiveEntry entry;
      while ((entry = tis.getNextTarEntry()) != null) {
        File newFile = new File(targetDir, entry.getName());
        if (entry.isDirectory()) {
          if (!newFile.exists() && !newFile.mkdirs()) {
            throw new IOException("Failed to create directory " + newFile.getAbsolutePath());
          }
        } else {
          File parent = newFile.getParentFile();
          if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent.getAbsolutePath());
          }
          try (FileOutputStream fos = new FileOutputStream(newFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = tis.read(buffer)) != -1) {
              fos.write(buffer, 0, len);
            }
          }
          // Set executable if the owner execute bit is set in the tar entry's mode
          // 0100 (octal) equals 64 in decimal.
          if ((entry.getMode() & 0100) != 0) {
            newFile.setExecutable(true, true);
          }
        }
      }
    }
  }
}
