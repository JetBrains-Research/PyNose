package pynose;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.impl.ProjectJdkTableImpl;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;

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
        return "pynose";
    }

    @Override
    public void main(@NotNull List<String> args) {
        System.out.println("args = " + args);
        if (args.size() != 4 || args.subList(1, args.size()).stream().anyMatch(String::isBlank)) {
            System.err.println("incorrect command line arguments");
            System.err.println("usage: test_smell projectPath pythonInterpreter outputDir");
            System.exit(0);
        }

        var path = args.get(1);
        if (path.charAt(path.length() - 1) == File.separatorChar) {
            path = path.substring(0, path.length() - 1);
        }
        var pythonInterpreter = args.get(2);
        var outputDir = args.get(3);
        if (outputDir.charAt(outputDir.length() - 1) == File.separatorChar) {
            outputDir = outputDir.substring(0, outputDir.length() - 1);
        }
        System.out.println("path = " + path);
        System.out.println("outputDir = " + outputDir);

        var splitter = File.separator.replace("\\", "\\\\");
        var pathComponents = path.split(splitter);
        var outputFileName = outputDir + File.separatorChar + pathComponents[pathComponents.length - 1] + ".json";
        System.out.println("outputFileName = " + outputFileName);

        var p = ProjectUtil.openOrImport(path, null, true);

        if (p == null) {
            System.out.println("project is null");
            System.exit(0);
        }
        var projectRootManager = ProjectRootManager.getInstance(p);
        if (projectRootManager.getProjectSdk() == null) {
            // the value depends on what Python interpreter you have on your computer
            var pythonSdk = ProjectJdkTableImpl.getInstance().findJdk(pythonInterpreter);
            if (pythonSdk == null) {
                System.out.println("Cannot find specified Python interpreter; printing available ones...");
                Arrays.stream(ProjectJdkTableImpl.getInstance().getAllJdks()).forEach(pythonCandidate ->
                        System.out.println("pythonCandidate = " + pythonCandidate)
                );
                System.out.println("If nothing printed, you may have to go to GUI mode to configure a Python interpreter");
                System.exit(0);
            }
            WriteAction.run(() -> projectRootManager.setProjectSdk(pythonSdk));
        }
        if (p.isInitialized()) {
            System.out.println("Project \"" + p.getName() + "\" is initialized");
            var fileResultArray = new JsonArray();
            var projectPsiFiles = Util.extractPsiFromProject(p);
            projectPsiFiles.forEach(psiFile -> {
                var testCaseResultArray = new JsonArray();
                Util.gatherTestCases(psiFile).forEach(testCase -> {
                    var testCaseResultObject = new JsonObject();
                    testCaseResultObject.addProperty("name", testCase.getName());
                    var detectorResultArray = new JsonArray();
                    Util.newAllDetectors(testCase).forEach(detector -> {
                        detector.analyze();
                        detectorResultArray.add(detector.getSmellDetailJSON());
                    });
                    testCaseResultObject.add("detectorResults", detectorResultArray);
                    var testMethodCount = Util.gatherTestMethods(testCase).size();
                    testCaseResultObject.addProperty("numberOfMethods", testMethodCount);
                    if (testMethodCount > 0) {
                        testCaseResultArray.add(testCaseResultObject);
                    }
                });
                if (testCaseResultArray.size() > 0) {
                    var pyFileResultObject = new JsonObject();
                    pyFileResultObject.addProperty("name", psiFile.getName());
                    pyFileResultObject.add("testCases", testCaseResultArray);
                    fileResultArray.add(pyFileResultObject);
                }
            });

            var jsonString = new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(JsonParser.parseString(fileResultArray.toString()));
            try {
                var bufferedWriter = new BufferedWriter(new FileWriter(outputFileName));
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
