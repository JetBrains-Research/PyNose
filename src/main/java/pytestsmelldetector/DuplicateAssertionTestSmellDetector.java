package pytestsmelldetector;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;

import java.util.*;

public class DuplicateAssertionTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(DuplicateAssertionTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasDuplicateAssert = new HashMap<>();
    private final Set<String> asserts = new HashSet<>();
    private final DuplicateAssertionVisitor visitor = new DuplicateAssertionVisitor();

    public DuplicateAssertionTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            asserts.clear();
            testHasDuplicateAssert.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testHasDuplicateAssert.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        testHasDuplicateAssert.clear();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", Util.mapToJsonArray(testHasDuplicateAssert, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return testHasDuplicateAssert.containsValue(true);
    }

    class DuplicateAssertionVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression) || !Util.isCallAssertMethod((PyReferenceExpression) child)) {
                return;
            }

            String assertionCall = callExpression.getText();
            if (asserts.contains(assertionCall)) {
                testHasDuplicateAssert.replace(currentMethod, true);
            } else {
                asserts.add(assertionCall);
            }
        }
    }
}
