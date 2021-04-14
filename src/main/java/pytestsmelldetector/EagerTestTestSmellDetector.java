package pytestsmelldetector;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EagerTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(EagerTestTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasEagerTestTestSmell;
    private final Map<String, String> eagerTestCheck;
    private final EagerTestVisitor visitor;

    public EagerTestTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        testHasEagerTestTestSmell = new HashMap<>();
        eagerTestCheck = new HashMap<>();
        visitor = new EagerTestVisitor();
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            eagerTestCheck.clear();
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        currentMethod = null;
        testHasEagerTestTestSmell.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        testHasEagerTestTestSmell.clear();
    }

    @Override
    public boolean hasSmell() {
        return testHasEagerTestTestSmell.containsValue(true);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", Util.mapToJsonArray(testHasEagerTestTestSmell, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    class EagerTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(@NotNull PyCallExpression callExpression) {
            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression)) {
                return;
            }

            PyReferenceExpression expression = (PyReferenceExpression) child;
            if (Util.isCallAssertMethod(expression)) {
                for (PsiElement element : callExpression.getChildren()) {
                    visitElement(element);
                }
                return;
            }

            PsiElement expressionFirstChild = expression.getFirstChild();
            if (!(expressionFirstChild instanceof PyReferenceExpression)) {
                return;
            }

            PyReferenceExpression qualifier = (PyReferenceExpression) expressionFirstChild;
            if (eagerTestCheck.containsKey(qualifier.getName())) {
                if (!eagerTestCheck.get(qualifier.getName()).equals(expression.getName())) {
                    testHasEagerTestTestSmell.put(currentMethod, true);
                }
            } else {
                eagerTestCheck.put(qualifier.getName(), expression.getName());
            }
            for (PsiElement element : callExpression.getChildren()) {
                visitElement(element);
            }
        }
    }
}
