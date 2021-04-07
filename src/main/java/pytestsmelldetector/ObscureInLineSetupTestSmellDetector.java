package pytestsmelldetector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyFunction;

import java.util.*;

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
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
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
    public String getSmellName() {
        return "ObscureInLineSetup";
    }

    @Override
    public boolean hasSmell() {
        return testMethodLocalVarCount.values().stream().anyMatch(s -> s.size() > 10);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        JsonArray detailArray = new JsonArray();
        testMethodLocalVarCount.forEach((pyFunction, strings) -> {
            JsonArray mapEntry = new JsonArray();
            mapEntry.add(pyFunction.getName());
            JsonArray localVars = new JsonArray();
            strings.forEach(localVars::add);
            mapEntry.add(localVars);
            detailArray.add(mapEntry);
        });
        jsonObject.add("detail", detailArray);
        return jsonObject;
    }

    class ObscureInLineSetupVisitor extends MyPsiElementVisitor {
        public void visitPyAssignmentStatement(PyAssignmentStatement assignmentStatement) {
            Set<String> localVars = testMethodLocalVarCount.get(currentMethod);
            for (PyExpression target : assignmentStatement.getTargets()) {
                if (target.getChildren().length == 0) {
                    localVars.add(target.getName());
                }
            }
        }
    }
}
