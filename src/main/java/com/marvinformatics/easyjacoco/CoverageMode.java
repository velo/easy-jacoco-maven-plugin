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

/**
 * Defines the mode for coverage report project generation.
 *
 * @since 1.1.0
 */
public enum CoverageMode {
  /**
   * Legacy mode: generates coverage project in target directory. The pom is completely replaced on
   * each build.
   *
   * @deprecated This mode will be removed in the next major version. Use {@link #PERSISTENT}
   *     instead.
   */
  @Deprecated
  LEGACY,

  /**
   * Persistent mode: generates coverage project in a configurable directory (default: coverage/).
   * The pom is merged with existing content and can be committed to version control.
   */
  PERSISTENT
}
