package com.marvinformatics.easyjacoco.jacoco;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A file filter using includes/excludes patterns.
 */
public class FileFilter {

	private static final String DEFAULT_INCLUDES = "**";
	private static final String DEFAULT_EXCLUDES = "";

	private final List<String> includes;
	private final List<String> excludes;

	/**
	 * Construct a new FileFilter
	 *
	 * @param includes list of includes patterns
	 * @param excludes list of excludes patterns
	 */
	public FileFilter(final List<String> includes, final List<String> excludes) {
		this.includes = includes;
		this.excludes = excludes;
	}

	/**
	 * Returns a list of files.
	 *
	 * @param directory the directory to scan
	 * @return a list of files
	 * @throws IOException if file system access fails
	 */
	public List<File> getFiles(final File directory) throws IOException {
		return getFiles(directory, getIncludes(), getExcludes());
	}

	public static List<File> getFiles(File directory, String includes, String excludes) throws IOException {
		var includePatterns = List.of(includes.split(","));
		var excludePatterns = excludes == null ? List.<String>of() : List.of(excludes.split(","));

		try (var stream = Files.walk(directory.toPath())) {
			return stream.filter(Files::isRegularFile).map(Path::toFile)
					.filter(file -> matches(file, includePatterns, true))
					.filter(file -> !matches(file, excludePatterns, false)).toList();
		}
	}

	private static boolean matches(File file, List<String> patterns, boolean defaultIfEmpty) {
		if (patterns.isEmpty())
			return defaultIfEmpty;
		String path = file.getPath().replace(File.separatorChar, '/');
		return patterns.stream().anyMatch(p -> path.matches(globToRegex(p.trim())));
	}

	private static String globToRegex(String glob) {
		String regex = "^" + glob.replace(".", "\\.").replace("**", ".+").replace("*", "[^/]*") + "$";
		return regex;
	}

	/**
	 * Get the includes pattern
	 *
	 * @return the pattern
	 */
	public String getIncludes() {
		return this.buildPattern(this.includes, DEFAULT_INCLUDES);
	}

	/**
	 * Get the excludes pattern
	 *
	 * @return the pattern
	 */
	public String getExcludes() {
		return this.buildPattern(this.excludes, DEFAULT_EXCLUDES);
	}

	private String buildPattern(final List<String> patterns, final String defaultPattern) {
		String pattern = defaultPattern;
		if (patterns != null && !patterns.isEmpty()) {
			pattern = patterns.stream().collect(Collectors.joining(","));
		}
		return pattern;
	}
}
