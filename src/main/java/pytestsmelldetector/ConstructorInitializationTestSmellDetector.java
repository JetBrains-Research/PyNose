package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;

import java.util.Arrays;
import java.util.Objects;

public class ConstructorInitializationTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(ConstructorInitializationTestSmellDetector.class);
    private boolean init;

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

    @Override
    public String getSmellName() {
        return "Constructor Initialization";
    }

    @Override
    public String getSmellDetail() {
        return Boolean.toString(init);
    }

    public boolean hasInit() {
        return init;
    }
}
