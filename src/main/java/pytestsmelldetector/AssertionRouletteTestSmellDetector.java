package pytestsmelldetector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import java.util.*;

public class AssertionRouletteTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(AssertionRouletteTestSmellDetector.class);
    private final Map<PyFunction, List<PyCallExpression>> assertionsInTests = new HashMap<>();
    private final Map<PyFunction, Boolean> testHasAssertionRoulette = new HashMap<>();
    private final AssertionRouletteVisitor visitor = new AssertionRouletteVisitor();

    public AssertionRouletteTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            testHasAssertionRoulette.put(currentMethod, false);
            assertionsInTests.put(currentMethod, new ArrayList<>());
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;

        for (PyFunction testMethod : assertionsInTests.keySet()) {

            List<PyCallExpression> calls = assertionsInTests.get(testMethod);

            if (calls.size() < 2) {
                continue;
            }

            for (PyCallExpression call : calls) {
                PyArgumentList argumentList = call.getArgumentList();
                if (argumentList == null) {
                    LOG.warn("assertion with no argument");
                    continue;
                }
                if (argumentList.getKeywordArgument("msg") != null) {
                    continue;
                }

                if (Util.ASSERT_METHOD_TWO_PARAMS.contains(((PyReferenceExpression) call.getFirstChild()).getName()) &&
                        argumentList.getArguments().length < 3) {
                    testHasAssertionRoulette.replace(testMethod, true);
                } else if (Util.ASSERT_METHOD_ONE_PARAM.containsKey(((PyReferenceExpression) call.getFirstChild()).getName()) &&
                        argumentList.getArguments().length < 2) {
                    testHasAssertionRoulette.replace(testMethod, true);
                }
            }
        }
    }

    @Override
    public void reset() {
        currentMethod = null;
        testHasAssertionRoulette.clear();
        assertionsInTests.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        testHasAssertionRoulette.clear();
        assertionsInTests.clear();
    }

    public Map<PyFunction, Boolean> getTestHasAssertionRoulette() {
        return testHasAssertionRoulette;
    }

    @Override
    public String getSmellName() {
        return "Assertion Roulette";
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        JsonArray detailArray = Util.mapToJsonArray(testHasAssertionRoulette, PyFunction::getName, Objects::toString);
        jsonObject.add("detail", detailArray);
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return testHasAssertionRoulette.containsValue(true);
    }

    class AssertionRouletteVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {

            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression) || !Util.isCallAssertMethod((PyReferenceExpression) child)) {
                return;
            }

            assertionsInTests.get(currentMethod).add(callExpression);
        }
    }
}
