package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyRaiseStatement;
import com.jetbrains.python.psi.PyTryExceptStatement;

import java.util.HashMap;
import java.util.Map;

public class ExceptionHandlingTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(ExceptionHandlingTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasExceptionHandlingLogic = new HashMap<>();
    private final ExceptionHandlingVisitor visitor;

    public ExceptionHandlingTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        visitor = new ExceptionHandlingVisitor();
    }

    @Override
    public void analyze() {
        for (PyFunction testMethod : Util.gatherTestMethods(testCase)) {
            currentMethod = testMethod;
            testHasExceptionHandlingLogic.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testHasExceptionHandlingLogic.clear();
        currentMethod = null;
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        reset();
    }

    @Override
    public String getSmellName() {
        return "Exception Handling";
    }

    @Override
    public String getSmellDetail() {
        return testHasExceptionHandlingLogic.toString();
    }

    public Map<PyFunction, Boolean> getTestHasExceptionHandlingLogic() {
        return testHasExceptionHandlingLogic;
    }

    class ExceptionHandlingVisitor extends MyPsiElementVisitor {
        public void visitPyTryExceptStatement(PyTryExceptStatement tryExceptStatement) {
            testHasExceptionHandlingLogic.put(currentMethod, true);
        }

        public void visitPyRaiseStatement(PyRaiseStatement raiseStatement) {
            testHasExceptionHandlingLogic.put(currentMethod, true);
        }
    }
}
