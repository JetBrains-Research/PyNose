package ui.toolWindow;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.psi.PyClass;
import pytestsmelldetector.AbstractTestSmellDetector;
import pytestsmelldetector.Util;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class MyToolWindow {

    private final Project project;
    private JButton refreshToolWindowButton;
    private JLabel resultLabel;
    private JPanel myToolWindowContent;

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
                    List<AbstractTestSmellDetector> allDetectors = Util.newAllDetectors(testCase);
                    for (AbstractTestSmellDetector detector : allDetectors) {
                        detector.analyze();
                        stringBuilder.append(detector.getSmellName()).append('\n');
                        stringBuilder.append(detector.getSmellDetail()).append("\n\n");
                    }
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
