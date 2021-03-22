import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pytestsmelldetector.Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PluginRunner implements ApplicationStarter {
    private static final Logger LOG = Logger.getInstance(PluginRunner.class);

    @Override
    public String getCommandName() {
        return "test_smell";
    }

    @Override
    public void main(@NotNull List<String> args) {
        String pathString = "C:\\Users\\tjwan\\PycharmProjects\\PythonTestSmellTestProject";
        Project p = ProjectUtil.openOrImport(pathString, null, true);

        if (p == null) {
            System.exit(1);
        }
        if (p.isInitialized()) {
            LOG.warn("Project \"" + p.getName() + "\" is initialized");

            StringBuilder stringBuilder = new StringBuilder();
            List<PsiFile> projectPsiFiles = Util.extractPsiFromProject(p);
            projectPsiFiles.forEach(psiFile -> {
                stringBuilder.append(psiFile.toString()).append('\n');
                Util.gatherTestCases(psiFile).forEach(testCase -> {
                    stringBuilder.append(testCase.toString()).append('\n');
                    Util.newAllDetectors(testCase).forEach(detector -> {
                        detector.analyze();
                        stringBuilder.append(detector.getSmellName()).append('\n');
                        stringBuilder.append(detector.getSmellDetail()).append("\n\n");
                    });
                });
                stringBuilder.append("\n\n");
            });

            LOG.warn(stringBuilder.toString());
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString + ".txt"));
                bufferedWriter.write(stringBuilder.toString());
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            LOG.warn("done");
        }
        ProjectManager.getInstance().addProjectManagerListener(p, new MyProjectManagerListener());
        System.exit(0);
    }
}

class MyProjectManagerListener implements ProjectManagerListener {
    private static final Logger LOG = Logger.getInstance(MyProjectManagerListener.class);

    @Override
    public void projectOpened(@NotNull Project project) {
        LOG.warn("Project \"" + project.getName() + "\" is opened");
        ProjectManager.getInstance().closeAndDispose(project);
    }

    @Override
    public void projectClosed(@NotNull Project project) {
        System.exit(0);
    }
}
