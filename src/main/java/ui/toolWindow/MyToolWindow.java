package ui.toolWindow;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ui.JBUI;
import com.jetbrains.python.psi.PyClass;
import pytestsmelldetector.AbstractTestSmellDetector;
import pytestsmelldetector.Util;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class MyToolWindow {
    private static final String CSS = "h2 { margin-left: 5px; } h3 { margin-left: 10px; }";
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
            stringBuilder.append("<html>");
            stringBuilder.append("<style>").append(CSS).append("</style>");
            stringBuilder.append("<body>");
            stringBuilder.append("<h1>Smells Detected by PyNose</h1>");

            for (VirtualFile f : files) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(f);

                if (psiFile == null) continue;

                StringBuilder fileHtml = new StringBuilder();
                List<PyClass> testCases = Util.gatherTestCases(psiFile);
                int totalSmellCount = 0;
                for (PyClass testCase : testCases) {
                    List<AbstractTestSmellDetector> allDetectors = Util.newAllDetectors(testCase);

                    StringBuilder ulli = new StringBuilder();
                    ulli.append("<ul>");
                    int smellCount = 0;
                    for (AbstractTestSmellDetector detector : allDetectors) {
                        detector.analyze();
                        boolean hasSmell = detector.hasSmell();
                        if (hasSmell) {
                            ulli.append("<li>")
                                    .append(detector.getSmellName())
                                    .append("</li>");
                            ++smellCount;
                        }
                    }
                    ulli.append("</ul>");
                    if (smellCount > 0) {
                        fileHtml.append("<div class=\"test-case\"><h3>").append(testCase.getName()).append("</h3>");
                        fileHtml.append(ulli);
                        fileHtml.append("</div>");
                    }
                    totalSmellCount += smellCount;
                }
                if (testCases.size() > 0 && totalSmellCount > 0) {
                    stringBuilder.append("<div class=\"test-file\">");
                    stringBuilder.append("<h2>").append(psiFile.getName()).append("</h2>");
                    stringBuilder.append(fileHtml);
                    stringBuilder.append("</div>");
                }
            }

            stringBuilder.append("</body>");
            stringBuilder.append("</html>");
            message = stringBuilder.toString();
        }

        resultLabel.setText(message);
        resultLabel.setBorder(JBUI.Borders.empty(10));
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

}
