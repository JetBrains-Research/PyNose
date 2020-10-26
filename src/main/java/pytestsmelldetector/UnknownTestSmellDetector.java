package pytestsmelldetector;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;

import java.util.HashMap;
import java.util.List;

public class UnknownTestSmellDetector extends AbstractSmellDetector {
    private PyClass testCase;
    private final HashMap<PyFunction, Integer> assertCounts;
    private PyFunction currentMethod;

    private static final Logger LOG = Logger.getInstance(UnknownTestSmellDetector.class);

    class UnknownTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression)) {
                return;
            }

            if (Util.isCallAssertMethod((PyReferenceExpression) child)) {
                assertCounts.put(currentMethod, assertCounts.get(currentMethod) + 1);
            }
        }
    }

    private final UnknownTestVisitor visitor;

    public UnknownTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        assertCounts = new HashMap<>();
        visitor = new UnknownTestVisitor();
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            assertCounts.put(currentMethod, 0);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        assertCounts.clear();
    }

    @Override
    public void reset(PyClass newTestCase) {
        testCase = newTestCase;
        assertCounts.clear();
    }

    public HashMap<PyFunction, Integer> getAssertCounts() {
        return assertCounts;
    }
}
