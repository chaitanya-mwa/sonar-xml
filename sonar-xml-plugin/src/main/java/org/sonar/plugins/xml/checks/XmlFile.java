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

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;

import com.google.common.io.Files;
import org.sonar.api.batch.fs.InputFile;

/**
 * Checks and analyzes report measurements, issues and other findings in WebSourceCode.
 *
 * @author Matthijs Galesloot
 */
public class XmlFile {

  private static final Logger LOG = LoggerFactory.getLogger(XmlFile.class);
  private static final String XML_PROLOG_START_TAG = "<?xml";

  private final InputFile inputFile;
  private File noCharBeforePrologFile;

  /**
   * Number of lines removed before xml prolog if present
   */
  private int lineDeltaForIssue = 0;
  private boolean hasCharsBeforeProlog = false;

  public XmlFile(InputFile inputFile) {
    this.inputFile = inputFile;
  }

  public String getFilePath() {
    return inputFile.absolutePath();
  }

  /**
   * Check if the xml file starts with a prolog "&lt?xml version="1.0" ?&gt"
   * if so, check if there is any characters prefixing it.
   */
  public void checkForCharactersBeforeProlog(FileSystem fileSystem) {
    if (inputFile.file() == null) {
      return;
    }

    try {
      int lineNb = 1;
      Pattern firstTagPattern = Pattern.compile("<[a-zA-Z?]+");

      for (String line : Files.readLines(inputFile.file(), fileSystem.encoding())) {
        Matcher m = firstTagPattern.matcher(line);
        if (m.find()) {
          int groupIndex = line.indexOf(m.group());

          if (XML_PROLOG_START_TAG.equals(m.group()) && (groupIndex > 0 || lineNb > 1)) {
            hasCharsBeforeProlog = true;
          }
          break;
        }
        lineNb++;
      }

      if (hasCharsBeforeProlog) {
        processCharBeforePrologInFile(fileSystem, lineNb);
      }
    } catch (IOException e) {
      LOG.warn("Unable to analyse file {}", inputFile.absolutePath(), e);
    }
  }

  /**
   * Create a temporary file without any character before the prolog and update the following
   * attributes in order to correctly report issues:
   * <ul>
   *   <li> lineDeltaForIssue
   *   <li> file
   */
  private void processCharBeforePrologInFile(FileSystem fileSystem, int lineDelta) {
    try {
      String content = Files.toString(inputFile.file(), fileSystem.encoding());
      File tempFile = new File(fileSystem.workDir(), inputFile.file().getName());

      int index = content.indexOf(XML_PROLOG_START_TAG);
      Files.write(content.substring(index), tempFile, fileSystem.encoding());

      noCharBeforePrologFile = tempFile;
      if (lineDelta > 1) {
        lineDeltaForIssue = lineDelta - 1;
      }

    } catch (IOException e) {
      LOG.warn("Unable to analyse file {}", inputFile.absolutePath(), e);
    }
  }

  public InputFile getInputFile() {
    return inputFile;
  }

  public int getLineDelta() {
    return lineDeltaForIssue;
  }

  public File getIOFile() {
    return noCharBeforePrologFile != null ? noCharBeforePrologFile : inputFile.file();
  }

  public int getPrologLine() {
    return lineDeltaForIssue + 1;
  }

  public boolean hasCharsBeforeProlog() {
    return hasCharsBeforeProlog;
  }
}
