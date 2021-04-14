package pytestsmelldetector;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import java.util.*;

public class MagicNumberTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(MagicNumberTestTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testMethodHasMagicNumber;
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

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", Util.mapToJsonArray(testMethodHasMagicNumber, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return testMethodHasMagicNumber.containsValue(true);
    }

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
}
