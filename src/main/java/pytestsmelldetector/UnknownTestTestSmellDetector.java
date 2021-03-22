package pytestsmelldetector;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;

import java.util.HashMap;
import java.util.List;

public class UnknownTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(UnknownTestTestSmellDetector.class);
    private final HashMap<PyFunction, Integer> assertCounts;
    private final UnknownTestVisitor visitor;

    public UnknownTestTestSmellDetector(PyClass aTestCase) {
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

    @Override
    public String getSmellName() {
        return "Unknown Test";
    }

    @Override
    public String getSmellDetail() {
        return assertCounts.toString();
    }

    public HashMap<PyFunction, Integer> getAssertCounts() {
        return assertCounts;
    }

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
}
