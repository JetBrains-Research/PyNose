package pynose.ui;


import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ui.JBUI;
import pynose.Util;

import javax.swing.*;

public class PyNoseGUI {
    private static final String CSS = "h2 { margin-left: 5px; } h3 { margin-left: 10px; }";
    private final Project project;
    private JButton refreshButton;
    private JLabel resultLabel;
    private JPanel content;

    public PyNoseGUI(Project theProject) {
        project = theProject;
        refreshButton.addActionListener(e -> updateResultLabel());
        updateResultLabel();
    }

    public void updateResultLabel() {
        String message;
        if (project == null) message = "No open project";
        else {
            var files = FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project));

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<html>");
            stringBuilder.append("<style>").append(CSS).append("</style>");
            stringBuilder.append("<body>");
            stringBuilder.append("<h1>Smells Detected by PyNose</h1>");

            for (var f : files) {
                var psiFile = PsiManager.getInstance(project).findFile(f);

                if (psiFile == null) continue;

                var fileHtml = new StringBuilder();
                var testCases = Util.gatherTestCases(psiFile);

                var totalSmellCount = 0;
                for (var testCase : testCases) {
                    var allDetectors = Util.newAllDetectors(testCase);
                    var ulli = new StringBuilder();
                    ulli.append("<ul>");

                    var smellCount = 0;
                    for (var detector : allDetectors) {
                        detector.analyze();
                        var hasSmell = detector.hasSmell();
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
        return content;
    }
}
