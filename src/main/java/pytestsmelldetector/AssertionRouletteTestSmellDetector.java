package pytestsmelldetector;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import java.util.*;

public class AssertionRouletteTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(AssertionRouletteTestSmellDetector.class);
    private final Map<PyFunction, List<PyCallExpression>> assertionCallsInTests = new HashMap<>();
    private final Map<PyFunction, List<PyAssertStatement>> assertStatementsInTests = new HashMap<>();
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
            assertionCallsInTests.put(currentMethod, new ArrayList<>());
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;

        for (PyFunction testMethod : assertionCallsInTests.keySet()) {

            List<PyCallExpression> calls = assertionCallsInTests.get(testMethod);

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

        for (PyFunction testMethod : assertStatementsInTests.keySet()) {
            List<PyAssertStatement> asserts = assertStatementsInTests.get(testMethod);
            if (asserts.size() < 2) {
                continue;
            }

            for (PyAssertStatement assertStatement : asserts) {
                PyExpression[] expressions = assertStatement.getArguments();
                if (expressions.length < 2) {
                    testHasAssertionRoulette.replace(testMethod, true);
                }
            }
        }
    }

    @Override
    public void reset() {
        currentMethod = null;
        testHasAssertionRoulette.clear();
        assertionCallsInTests.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        reset();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        JsonObject detail = new JsonObject();
        detail.add("assertionCallsInTests", Util.mapToJsonArray(assertionCallsInTests, PyFunction::getName, Objects::toString));
        detail.add("assertStatementsInTests", Util.mapToJsonArray(assertStatementsInTests, PyFunction::getName, Objects::toString));
        jsonObject.add("detail", detail);
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

            assertionCallsInTests.get(currentMethod).add(callExpression);
        }

        public void visitPyAssertStatement(PyAssertStatement assertStatement) {
            assertStatementsInTests.get(currentMethod).add(assertStatement);
        }
    }
}
