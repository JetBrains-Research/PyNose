package pynose;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObscureInLineSetupTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOGGER = Logger.getInstance(ObscureInLineSetupTestSmellDetector.class);
    private final Map<PyFunction, Set<String>> testMethodLocalVarCount = new HashMap<>();
    private final ObscureInLineSetupVisitor visitor = new ObscureInLineSetupVisitor();
    private PyClass testCase;

    public ObscureInLineSetupTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        var testMethods = Util.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
            currentMethod = testMethod;
            testMethodLocalVarCount.put(currentMethod, new HashSet<>());
            visitor.visitElement(testMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testMethodLocalVarCount.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        reset();
    }

    @Override
    public boolean hasSmell() {
        return testMethodLocalVarCount.values().stream().anyMatch(s -> s.size() > 10);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        JsonArray detailArray = new JsonArray();
        testMethodLocalVarCount.forEach((pyFunction, strings) -> {
            var mapEntry = new JsonArray();
            mapEntry.add(pyFunction.getName());
            var localVars = new JsonArray();
            strings.forEach(localVars::add);
            mapEntry.add(localVars);
            detailArray.add(mapEntry);
        });
        jsonObject.add("detail", detailArray);
        return jsonObject;
    }

    class ObscureInLineSetupVisitor extends MyPsiElementVisitor {
        public void visitPyAssignmentStatement(PyAssignmentStatement assignmentStatement) {
            var localVars = testMethodLocalVarCount.get(currentMethod);
            for (var target : assignmentStatement.getTargets()) {
                if (target.getChildren().length == 0) {
                    localVars.add(target.getName());
                }
            }
        }
    }
}
