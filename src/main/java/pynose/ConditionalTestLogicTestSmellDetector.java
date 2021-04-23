package pynose;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConditionalTestLogicTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(ConditionalTestLogicTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasConditionalTestLogic = new HashMap<>();
    private final Map<PyFunction, Boolean> testHasComprehension = new HashMap<>();
    private final ConditionalTestLogicVisitor visitor = new ConditionalTestLogicVisitor();

    public ConditionalTestLogicTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        var testMethods = Util.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
            currentMethod = testMethod;
            testHasConditionalTestLogic.put(currentMethod, false);
            testHasComprehension.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testHasConditionalTestLogic.clear();
        testHasComprehension.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        testHasConditionalTestLogic.clear();
        testHasComprehension.clear();
    }

    @Override
    public boolean hasSmell() {
        return testHasConditionalTestLogic.containsValue(true) || testHasComprehension.containsValue(true);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        var detail = new JsonObject();
        detail.add("testHasConditionalTestLogic", Util.mapToJsonArray(testHasConditionalTestLogic, PyFunction::getName, Objects::toString));
        detail.add("testHasComprehension", Util.mapToJsonArray(testHasComprehension, PyFunction::getName, Objects::toString));
        jsonObject.add("detail", detail);
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

        public void visitPyListCompExpression(PyListCompExpression listCompExpression) {
            testHasComprehension.put(currentMethod, true);
        }

        public void visitPySetCompExpression(PySetCompExpression setCompExpression) {
            testHasComprehension.put(currentMethod, true);
        }

        public void visitPyDictCompExpression(PyDictCompExpression dictCompExpression) {
            testHasComprehension.put(currentMethod, true);
        }

        public void visitPyGeneratorExpression(PyGeneratorExpression generatorExpression) {
            testHasComprehension.put(currentMethod, true);
        }
    }
}
