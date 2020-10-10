package pytestsmelldetector;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import java.util.HashMap;
import java.util.List;

public class UnknownTestSmellDetector extends AbstractSmellDetector {
    private PyClass testCase;
    private HashMap<PyFunction, Integer> assertCounts;
    private PyFunction currentMethod;

    private static final Logger LOG = Logger.getInstance(UnknownTestSmellDetector.class);

    class UnknownTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression)) {
                return;
            }

            PyReferenceExpression calledMethodRef = (PyReferenceExpression) child;

            // TODO: do some real analysis instead of just based on name
            if (calledMethodRef.getText().startsWith("self.assert") || calledMethodRef.getText().startsWith("self.fail")) {
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
        assertCounts = new HashMap<>();
    }

    @Override
    public void reset(PyClass newTestCase) {
        testCase = newTestCase;
        assertCounts = new HashMap<>();
    }

    public HashMap<PyFunction, Integer> getAssertCounts() {
        return assertCounts;
    }
}
