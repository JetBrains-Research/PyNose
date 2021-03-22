package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.*;

import java.util.*;

public class ObscureInLineSetupTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOGGER = Logger.getInstance(ObscureInLineSetupTestSmellDetector.class);

    class ObscureInLineSetupVisitor extends MyPsiElementVisitor {
        public void visitPyAssignmentStatement(PyAssignmentStatement assignmentStatement) {
            Set<String> localVars = testMethodLocalVarCount.get(currentMethod);
            for (PyExpression target : assignmentStatement.getTargets()) {
                if (target.getChildren().length == 0) {
                    localVars.add(target.getName());
                }
            }
        }
    }

    private PyClass testCase;
    private final Map<PyFunction, Set<String>> testMethodLocalVarCount = new HashMap<>();
    private final ObscureInLineSetupVisitor visitor = new ObscureInLineSetupVisitor();

    public ObscureInLineSetupTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            testMethodLocalVarCount.put(currentMethod, new HashSet<>());
            visitor.visitElement(testMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testMethodLocalVarCount.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        reset();
    }

    @Override
    public String getSmellName() {
        return "ObscureInLineSetup";
    }

    @Override
    public String getSmellDetail() {
        return testMethodLocalVarCount.toString();
    }
}
