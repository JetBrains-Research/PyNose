import com.google.gson.*;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.ProjectJdkTableImpl;
import com.intellij.openapi.roots.ProjectRootManager;
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
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(p);
        if (projectRootManager.getProjectSdk() == null) {
            // the value depends on what Python interpreter you have on your computer
            Sdk pythonSdk = ProjectJdkTableImpl.getInstance().findJdk("Python 3.9");
            WriteAction.run(() -> projectRootManager.setProjectSdk(pythonSdk));
        }
        if (p.isInitialized()) {
            System.out.println("Project \"" + p.getName() + "\" is initialized");
            JsonArray jsonArray = new JsonArray();
            List<PsiFile> projectPsiFiles = Util.extractPsiFromProject(p);
            projectPsiFiles.forEach(psiFile -> {
                JsonArray testCaseResultArray = new JsonArray();
                Util.gatherTestCases(psiFile).forEach(testCase -> {
                    JsonObject testCaseResultObject = new JsonObject();
                    testCaseResultObject.addProperty("name", testCase.getName());
                    JsonArray detectorResultArray = new JsonArray();
                    Util.newAllDetectors(testCase).forEach(detector -> {
                        detector.analyze();
                        detectorResultArray.add(detector.getSmellDetailJSON());
                    });
                    testCaseResultObject.add("detectorResults", detectorResultArray);
                    testCaseResultArray.add(testCaseResultObject);
                });
                if (testCaseResultArray.size() > 0) {
                    JsonObject pyFileResultObject = new JsonObject();
                    pyFileResultObject.addProperty("name", psiFile.getName());
                    pyFileResultObject.add("testCases", testCaseResultArray);
                    jsonArray.add(pyFileResultObject);
                }
            });

            String jsonString = new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(JsonParser.parseString(jsonArray.toString()));
            System.out.println(jsonString);
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathString + ".json"));
                bufferedWriter.write(jsonString);
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("done");
        }
        System.exit(0);
    }
}
