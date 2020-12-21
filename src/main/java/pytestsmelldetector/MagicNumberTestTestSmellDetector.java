package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagicNumberTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(MagicNumberTestTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testMethodHasMagicNumber;

    class MagicNumberTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression) || !Util.isCallAssertMethod((PyReferenceExpression) child)) {
                return;
            }

            if (Arrays.stream(callExpression.getArguments()).anyMatch(PyNumericLiteralExpression.class::isInstance)) {
                testMethodHasMagicNumber.put(currentMethod, true);
            }
        }
    }

    private final MagicNumberTestVisitor visitor;

    public MagicNumberTestTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodHasMagicNumber = new HashMap<>();
        visitor = new MagicNumberTestVisitor();
        currentMethod = null;
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            testMethodHasMagicNumber.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testMethodHasMagicNumber.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodHasMagicNumber.clear();
    }

    public Map<PyFunction, Boolean> getTestMethodHasMagicNumber() {
        return testMethodHasMagicNumber;
    }
}
