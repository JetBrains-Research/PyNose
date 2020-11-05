package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyPassStatement;
import com.jetbrains.python.psi.PyStatement;

import java.util.HashMap;
import java.util.List;

public class EmptyTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(EmptyTestTestSmellDetector.class);

    class EmptyTestVisitor extends MyPsiElementVisitor {
        public void visitPyFunction(PyFunction testMethod) {
            PyStatement[] statements = testMethod.getStatementList().getStatements();
            testMethodEmptiness.replace(
                    currentMethod,
                    statements.length == 1 && (statements[0] instanceof PyPassStatement)
            );
        }
    }

    private PyClass testCase;
    private final HashMap<PyFunction, Boolean> testMethodEmptiness;
    private PyFunction currentMethod;
    private final EmptyTestVisitor visitor;

    public EmptyTestTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodEmptiness = new HashMap<>();
        visitor = new EmptyTestVisitor();
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            testMethodEmptiness.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testMethodEmptiness.clear();
        currentMethod = null;
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodEmptiness.clear();
        currentMethod = null;
    }

    public HashMap<PyFunction, Boolean> getTestMethodEmptiness() {
        return testMethodEmptiness;
    }
}
