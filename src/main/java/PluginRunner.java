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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PluginRunner implements ApplicationStarter {
    private static final Logger LOG = Logger.getInstance(PluginRunner.class);

    @Override
    public String getCommandName() {
        return "test_smell";
    }

    @Override
    public void main(@NotNull List<String> args) {
        System.out.println("args = " + args);
        if (args.size() != 4) {
            System.err.println("incorrect command line arguments");
            System.err.println("usage: test_smell projectPath pythonInterpreter outputDir");
            System.exit(0);
        }

        String path = args.get(1);
        if (path.charAt(path.length() - 1) == File.separatorChar) {
            path = path.substring(0, path.length() - 1);
        }
        String pythonInterpreter = args.get(2);
        String outputDir = args.get(3);
        if (outputDir.charAt(outputDir.length() - 1)  == File.separatorChar) {
            outputDir = outputDir.substring(0, outputDir.length() - 1);
        }
        System.out.println("path = " + path);
        System.out.println("outputDir = " + outputDir);

        String splitter = File.separator.replace("\\","\\\\");
        String[] pathComponents = path.split(splitter);
        String outputFileName = outputDir + File.separatorChar + pathComponents[pathComponents.length - 1] + ".json";
        System.out.println("outputFileName = " + outputFileName);

        Project p = ProjectUtil.openOrImport(path, null, true);

        if (p == null) {
            System.exit(1);
        }
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(p);
        if (projectRootManager.getProjectSdk() == null) {
            // the value depends on what Python interpreter you have on your computer
            Sdk pythonSdk = ProjectJdkTableImpl.getInstance().findJdk(pythonInterpreter);
            if (pythonSdk == null) {
                Arrays.stream(ProjectJdkTableImpl.getInstance().getAllJdks()).forEach(pythonCandidate ->
                    System.out.println("pythonCandidate = " + pythonCandidate)
                );
                System.exit(0);
            }
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
            try {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName));
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
