/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.xml.checks;

import java.io.FileNotFoundException;

import org.junit.Test;

/**
 * @author Matthijs Galesloot
 */
public class IndentCheckTest extends AbstractCheckTester {

  @Test
  public void checkIndent() throws FileNotFoundException {
    String fragment = "<html>\n<body><br>hello</br></body>\n</html>";
    parseCheckAndAssert(fragment, IndentCheck.class, 2);
  }

  @Test
  public void checkIndentTabs() throws FileNotFoundException {
    String fragment = "<html>\n\t<body>\t\t<br>hello</br>\t</body>\n</html>";
    parseCheckAndAssert(fragment, IndentCheck.class, 0);
  }
}
