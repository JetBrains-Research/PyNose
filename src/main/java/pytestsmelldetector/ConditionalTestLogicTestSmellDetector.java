package pytestsmelldetector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConditionalTestLogicTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(ConditionalTestLogicTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasConditionalTestLogic = new HashMap<>();
    private final ConditionalTestLogicVisitor visitor = new ConditionalTestLogicVisitor();

    public ConditionalTestLogicTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            testHasConditionalTestLogic.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testHasConditionalTestLogic.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        testHasConditionalTestLogic.clear();
    }

    public Map<PyFunction, Boolean> getTestHasConditionalTestLogic() {
        return testHasConditionalTestLogic;
    }

    @Override
    public String getSmellName() {
        return "Conditional Test Logic";
    }

    @Override
    public boolean hasSmell() {
        return testHasConditionalTestLogic.containsValue(true);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        JsonArray detailArray = Util.mapToJsonArray(testHasConditionalTestLogic, PyFunction::getName, Objects::toString);
        jsonObject.add("detail", detailArray);
        return jsonObject;
    }

    class ConditionalTestLogicVisitor extends MyPsiElementVisitor {
        public void visitPyIfStatement(PyIfStatement ifStatement) {
            testHasConditionalTestLogic.put(currentMethod, true);
        }

        public void visitPyForStatement(PyForStatement forStatement) {
            testHasConditionalTestLogic.put(currentMethod, true);
        }

        public void visitPyWhileStatement(PyWhileStatement whileStatement) {
            testHasConditionalTestLogic.put(currentMethod, true);
        }
    }
}
