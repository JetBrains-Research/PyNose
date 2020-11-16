package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStatement;

import java.util.Arrays;
import java.util.Objects;

public class ConstructorInitializationTestSmellDetector extends AbstractTestSmellDetector {
    private boolean init;

    private static final Logger LOG = Logger.getInstance(ConstructorInitializationTestSmellDetector.class);

    public ConstructorInitializationTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        init = false;
    }

    @Override
    public void analyze() {
        init = Arrays.stream(testCase.getStatementList().getStatements())
                .filter(PyFunction.class::isInstance)
                .map(PyFunction.class::cast).anyMatch(pyFunction -> Objects.equals(pyFunction.getName(), "__init__"));
    }

    @Override
    public void reset() {
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
    }

    public boolean hasInit() {
        return init;
    }
}
