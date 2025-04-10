package com.marvinformatics.easyjacoco;

import java.util.List;
import java.util.stream.Collectors;

import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.check.Limit;
import org.jacoco.report.check.Rule;

/**
 * Wrapper for {@link Rule} objects to allow Maven style includes/excludes lists
 *
 */
public class RuleConfiguration {

	public final Rule rule;

	/**
	 * Create a new configuration instance.
	 */
	public RuleConfiguration() {
		rule = new Rule();
	}

	/**
	 * @param element
	 *            element type this rule applies to TODO: use ElementType
	 *            directly once Maven 3 is required.
	 */
	public void setElement(final String element) {
		rule.setElement(ElementType.valueOf(element));
	}

	/**
	 * @param includes
	 *            includes patterns
	 */
	public void setIncludes(final List<String> includes) {
		rule.setIncludes(includes.stream().collect(Collectors.joining( ":")));
	}

	/**
	 *
	 * @param excludes
	 *            excludes patterns
	 */
	public void setExcludes(final List<String> excludes) {
		rule.setExcludes(excludes.stream().collect(Collectors.joining( ":")));
	}

	/**
	 * @param limits
	 *            list of {@link Limit}s configured for this rule
	 */
	public void setLimits(final List<Limit> limits) {
		rule.setLimits(limits);
	}

}