import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pytestsmelldetector.Util;

import java.util.List;

public class PluginRunner implements ApplicationStarter {
    private static final Logger LOG = Logger.getInstance(PluginRunner.class);

    @Override
    public String getCommandName() {
        return "test_smell";
    }

    @Override
    public void main(@NotNull List<String> args) {
        Project p = ProjectUtil.openOrImport("C:\\Users\\tjwan\\PycharmProjects\\SamplePythonProject - Copy", null, true);

        if (p == null) {
            System.exit(1);
        }
        if (p.isInitialized()) {
            LOG.warn("Project \"" + p.getName() + "\" is initialized");

            StringBuilder stringBuilder = new StringBuilder();
            List<PsiFile> projectPsiFiles = Util.extractPsiFromProject(p);
            LOG.warn(projectPsiFiles.toString());
            projectPsiFiles.forEach(psiFile ->
                    Util.gatherTestCases(psiFile).forEach(testCase ->
                            Util.newAllDetectors(testCase).forEach(detector -> {
                                detector.analyze();
                                stringBuilder.append(detector.getSmellName()).append('\n');
                                stringBuilder.append(detector.getSmellDetail()).append("\n\n");
                            })));

            LOG.warn(stringBuilder.toString());
            LOG.warn("done");
        }
        ProjectManager.getInstance().addProjectManagerListener(p, new MyProjectManagerListener());
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