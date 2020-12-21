// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.intellij.sdk.pycharm;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import pytestsmelldetector.*;

import java.util.Collection;

/**
 * Action class to demonstrate how to interact with the IntelliJ Platform.
 * The only action this class performs is to provide the user with a popup dialog as feedback.
 * Typically this class is instantiated by the IntelliJ Platform framework based on declarations
 * in the plugin.xml file. But when added at runtime this class is instantiated by an action group.
 */
public class PopupDialogAction extends AnAction {

  /**
   * Gives the user feedback when the dynamic action menu is chosen.
   * Pops a simple message dialog. See the psi_demo plugin for an
   * example of how to use AnActionEvent to access data.
   *
   * @param event Event received when the associated menu item is chosen.
   */
  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    Project project = event.getProject();
    String message;
    if (project == null) message = "No open project";
    else {
      Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project));

      StringBuilder stringBuilder = new StringBuilder();

      for (VirtualFile f : files) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(f);

        if (psiFile == null) continue;

        for (PyClass testCase : Util.gatherTestCases(psiFile)) {
          EagerTestTestSmellDetector detector = new EagerTestTestSmellDetector(testCase);
          detector.analyze();
          stringBuilder.append(testCase.getName())
                  .append("[EagerTestTestSmellDetector:\"")
                  .append(detector.getTestHasEagerTestTestSmell())
                  .append("\"]\n");
        }
      }

      message = stringBuilder.toString();
    }

    Messages.showMessageDialog(project,
            message,
            "Greetings from PyCharm Basics Plugin",
            Messages.getInformationIcon());
  }

  /**
   * Determines whether this menu item is available for the current context.
   * Requires a project to be open.
   *
   * @param e Event received when the associated group-id menu is chosen.
   */
  @Override
  public void update(AnActionEvent e) {
    // Set the availability based on whether a project is open
    Project project = e.getProject();
    e.getPresentation().setEnabledAndVisible(project != null);
  }
}
