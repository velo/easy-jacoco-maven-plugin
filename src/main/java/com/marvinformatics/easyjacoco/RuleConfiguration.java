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

import java.util.List;
import java.util.stream.Collectors;
import org.jacoco.core.analysis.ICoverageNode.ElementType;
import org.jacoco.report.check.Limit;
import org.jacoco.report.check.Rule;

/** Wrapper for {@link Rule} objects to allow Maven style includes/excludes lists */
public class RuleConfiguration {

  public final Rule rule;

  /** Create a new configuration instance. */
  public RuleConfiguration() {
    rule = new Rule();
  }

  /**
   * @param element element type this rule applies to TODO: use ElementType directly once Maven 3 is
   *     required.
   */
  public void setElement(final String element) {
    rule.setElement(ElementType.valueOf(element));
  }

  /**
   * @param includes includes patterns
   */
  public void setIncludes(final List<String> includes) {
    rule.setIncludes(includes.stream().collect(Collectors.joining(":")));
  }

  /**
   * @param excludes excludes patterns
   */
  public void setExcludes(final List<String> excludes) {
    rule.setExcludes(excludes.stream().collect(Collectors.joining(":")));
  }

  /**
   * @param limits list of {@link Limit}s configured for this rule
   */
  public void setLimits(final List<Limit> limits) {
    rule.setLimits(limits);
  }
}
