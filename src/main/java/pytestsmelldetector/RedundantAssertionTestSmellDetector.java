package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedundantAssertionTestSmellDetector extends AbstractSmellDetector {
    private PyClass testCase;
    private final HashMap<PyFunction, Integer> testMethodHaveRedundantAssertion;
    private PyFunction currentMethod;

    private static final Logger LOG = Logger.getInstance(RedundantAssertionTestSmellDetector.class);

    class RedundantAssertionVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression) || !Util.isCallAssertMethod((PyReferenceExpression) child)) {
                return;
            }

            List<PyExpression> argList = callExpression.getArguments(null);
            if (ASSERT_METHOD_ONE_PARAM.containsKey(((PyReferenceExpression) child).getName())) {
                if (argList.get(0).getText().equals(ASSERT_METHOD_ONE_PARAM.get(((PyReferenceExpression) child).getName()))) {
                    testMethodHaveRedundantAssertion.replace(
                            currentMethod,
                            testMethodHaveRedundantAssertion.get(currentMethod) + 1
                    );
                }
            } else if (ASSERT_METHOD_TWO_PARAMS.contains(((PyReferenceExpression) child).getName())) {
                if (argList.get(0).getText().equals(argList.get(1).getText())) {
                    testMethodHaveRedundantAssertion.replace(
                            currentMethod,
                            testMethodHaveRedundantAssertion.get(currentMethod) + 1
                    );
                }
            }
        }
    }

    private final RedundantAssertionVisitor visitor = new RedundantAssertionVisitor();

    public RedundantAssertionTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodHaveRedundantAssertion = new HashMap<>();
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            testMethodHaveRedundantAssertion.put(currentMethod, 0);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        currentMethod = null;
        testMethodHaveRedundantAssertion.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        testMethodHaveRedundantAssertion.clear();
    }

    public HashMap<PyFunction, Integer> getTestMethodHaveRedundantAssertion() {
        return testMethodHaveRedundantAssertion;
    }

    private final static List<String> ASSERT_METHOD_TWO_PARAMS = Arrays.asList(
            "assertEqual",
            "assertNotEqual",
            "assertIs",
            "assertIsNot",
            "assertAlmostEqual",
            "assertNotAlmostEqual",
            "assertGreater",
            "assertGreaterEqual",
            "assertLess",
            "assertLessEqual",
            "assertCountEqual",
            "assertMultiLineEqual",
            "assertSequenceEqual",
            "assertListEqual",
            "assertTupleEqual",
            "assertSetEqual",
            "assertDictEqual"
    );

    private final static Map<String, String> ASSERT_METHOD_ONE_PARAM = new HashMap<>();
    static {
        ASSERT_METHOD_ONE_PARAM.put("assertTrue", "True");
        ASSERT_METHOD_ONE_PARAM.put("assertFalse", "False");
        ASSERT_METHOD_ONE_PARAM.put("assertIsNone", "None");
        ASSERT_METHOD_ONE_PARAM.put("assertIsNotNone", "None");
    }
}
