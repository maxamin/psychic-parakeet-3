/**
 * This file is part of Eclipse Steady.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.kb.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.ConstructChange;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.FileAnalyzerFactory;
import org.eclipse.steady.kb.model.Commit;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.json.model.FileChange;

/**
 * Identifying all the constructs
 */
public class ConstructSet {
  private static final String COMMIT_BRANCH_SPLIT = ":";
  private static final String BEFORE_FOLDER = "before";
  private static final String AFTER_FOLDER = "after";
  private static final Set<String> SOURCE_EXTS =
      new HashSet<String>(Arrays.asList(new String[] {"java", "py"}));

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * Identifies all constructs that have been changed in the given revision. Getting the changed
   * sets of before and after commit, and afterwards compared according to the syntax of the
   * respective programming language.
   *
   * @param _commit a {@link org.eclipse.steady.kb.model.Commit} object.
   * @param _changes a {@link java.util.Set} object.
   * @return a {@link java.util.Set} object.
   */
  public static Set<ConstructChange> identifyConstructChanges(
      Commit _commit, Map<String, Set<ConstructChange>> _changes) {
    String commitId = _commit.getCommitId();
    String branch = _commit.getBranch();
    String timeStamp = _commit.getTimestamp();
    String repoUrl = _commit.getRepoUrl();
    String rev = commitId + branch;
    Set<ConstructChange> ch = _changes.get(rev);
    if (ch == null) {
      ch = new TreeSet<ConstructChange>();

      // Get and loop over all changed files
      final Set<FileChange> file_changes = getFileChanges(_commit, repoUrl);
      for (FileChange c : file_changes) {
        try {
          // Check if the file ext is supported
          if (SOURCE_EXTS.contains(c.getFileExtension())
              && FileAnalyzerFactory.isSupportedFileExtension(c.getFileExtension())) {
            final FileComparator comparator = new FileComparator(c, commitId, timeStamp);
            ch.addAll(comparator.identifyChanges());
          }
        } catch (Exception e) {
          ConstructSet.log.error("Error while analyzing {} : {}", c, e.getMessage());
        }
      }

      // remove MOD classes if no MOD method is included (excluding modification in inner
      // classes)
      // ConstructChange[] cc = (ConstructChange[]) ch.toArray();
      ConstructChange[] ch_array = ch.toArray(new ConstructChange[ch.size()]);
      for (ConstructChange c : ch_array) {
        ConstructId tocheck = c.getConstruct().getId();
        boolean toDelete = true;
        if (tocheck.getSharedType() == ConstructType.CLAS) {
          for (ConstructChange in : ch_array) {
            if (tocheck.equals(in.getConstruct().getId().getDefinitionContext())) {
              toDelete = false;
              break;
            }
          }
          if (toDelete) {
            ch.remove(c);
            ConstructSet.log.info(
                "Class [{}] removed from changeList as no METH/CONS included", tocheck.toString());
          }
        }
      }
      _changes.put(rev, ch);
    }
    return ch;
  }

  /**
   * get file changes for a commit
   *
   * @param _commit a {@link org.eclipse.steady.kb.model.Commit} object.
   * @param _rev a {@link java.lang.String} object.
   * @param _url a {@link java.lang.String} object.
   * @return a {@link java.util.Set} object.
   */
  private static Set<FileChange> getFileChanges(final Commit _commit, String _url) {
    String directory = _commit.getDirectory();
    final Path beforePath = Paths.get(directory, BEFORE_FOLDER);
    final Path afterPath = Paths.get(directory, AFTER_FOLDER);

    // Ensure that before and after folders exist
    if (!beforePath.toFile().exists()) {
      ConstructSet.log.error("Path with pre-commit files [{}] does not exists", beforePath);
      return Collections.emptySet();
    } else if (!afterPath.toFile().exists()) {
      ConstructSet.log.error("Path with post-commit files [{}] does not exists", afterPath);
      return Collections.emptySet();
    }

    final Set<FileChange> filesChanged = new HashSet<>();
    final List<String> filesModifiedOrDeleted = new ArrayList<>();

    // Search for files that were changed or deleted by the commit
    final Collection<File> beforeFiles = FileUtils.listFiles(beforePath.toFile(), null, true);
    String branch = _commit.getBranch();
    for (File file : beforeFiles) {
      if (file.isFile()) {
        final Path rel_path = beforePath.relativize(file.toPath());
        final Path afterFile = afterPath.resolve(rel_path);
        if (afterFile.toFile().exists()) {
          filesChanged.add(
              new FileChange(
                  _url,
                  branch + COMMIT_BRANCH_SPLIT + rel_path.toString(),
                  file,
                  afterFile.toFile()));
        } else {
          filesChanged.add(
              new FileChange(_url, rel_path.toString().replace('\\', '/'), file, null));
        }
        filesModifiedOrDeleted.add(rel_path.toString());
      }
    }

    // Search for files that were added by the commit
    final Collection<File> afterFiles = FileUtils.listFiles(afterPath.toFile(), null, true);
    for (File file : afterFiles) {
      if (file.isFile()) {
        final Path rel_path = afterPath.relativize(file.toPath());
        if (!filesModifiedOrDeleted.contains(rel_path.toString())) {
          filesChanged.add(
              new FileChange(
                  _url,
                  branch + COMMIT_BRANCH_SPLIT + rel_path.toString().replace('\\', '/'),
                  null,
                  file));
        }
      }
    }

    return filesChanged;
  }

  /**
   * Returns the union of changes for the given revisions. If null is passed as argument, all
   * revisions will be returned. If an empty array is passed, an empty set will be returned.
   *
   * @return a {@link java.util.Set} object.
   * @param _revs a {@link java.util.List} object
   * @param _changes a {@link java.util.Map} object
   */
  public static Set<ConstructChange> getConsolidatedChanges(
      List<Commit> _revs, Map<String, Set<ConstructChange>> _changes) {
    final Set<ConstructChange> ch = new TreeSet<ConstructChange>();
    for (Entry<String, Set<ConstructChange>> entry : _changes.entrySet()) {
      if (_revs == null) ch.addAll(_changes.get(entry.getKey()));
      else {
        for (Commit rev : _revs) {
          String commitAndBranch = rev.getCommitId() + rev.getBranch();
          if (entry.getKey().equals(commitAndBranch)) ch.addAll(_changes.get(entry.getKey()));
        }
      }
    }
    return ch;
  }
}
