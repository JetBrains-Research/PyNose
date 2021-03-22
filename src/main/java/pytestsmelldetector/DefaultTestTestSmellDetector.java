package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.PyClass;

import java.util.Objects;

/**
 * Created with IntelliJ IDEA on MacBook.
 * Description:
 * User: Tongjie Wang
 * Date: 2020-11-15
 * Time: 9:13 PM
 */
public class DefaultTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(DefaultTestTestSmellDetector.class);
    private boolean isDefaultTest;

    public DefaultTestTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        isDefaultTest = false;
    }

    @Override
    public void analyze() {
        isDefaultTest = Objects.equals(testCase.getName(), "MyTestCase");
    }

    @Override
    public void reset() {
        isDefaultTest = false;
    }

    @Override
    public void reset(PyClass aTestCase) {
        isDefaultTest = false;
        testCase = aTestCase;
    }

    @Override
    public String getSmellName() {
        return "Default Test";
    }

    @Override
    public String getSmellDetail() {
        return Boolean.toString(isDefaultTest);
    }

    public boolean isDefaultTest() {
        return isDefaultTest;
    }
}
