// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.intellij.sdk.toolWindow;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.psi.PyClass;
import pytestsmelldetector.SleepyTestTestSmellDetector;
import pytestsmelldetector.Util;

import javax.swing.*;
import java.util.Collection;

public class MyToolWindow {

  private JButton refreshToolWindowButton;
  private JLabel resultLabel;
  private JPanel myToolWindowContent;
  private final Project project;

  public MyToolWindow(Project theProject) {
    project = theProject;
    refreshToolWindowButton.addActionListener(e -> updateResultLabel());
    updateResultLabel();
  }

  public void updateResultLabel() {
    String message;
    if (project == null) message = "No open project";
    else {
      Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project));

      StringBuilder stringBuilder = new StringBuilder();

      for (VirtualFile f : files) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(f);

        if (psiFile == null) continue;

        for (PyClass testCase : Util.gatherTestCases(psiFile)) {
          SleepyTestTestSmellDetector detector = new SleepyTestTestSmellDetector(testCase);
          detector.analyze();
          stringBuilder.append(testCase.getName())
                  .append('[').append(SleepyTestTestSmellDetector.class.toString())
                  .append(":\"")
                  .append(detector.getTestHasSleepWithoutComment())
                  .append("\"]\n");
        }
      }

      message = stringBuilder.toString();
    }

    resultLabel.setText(message);
  }

  public JPanel getContent() {
    return myToolWindowContent;
  }

}
