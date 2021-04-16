package pytestsmelldetector;


import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UnknownTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(UnknownTestTestSmellDetector.class);
    private final HashMap<PyFunction, Integer> assertCounts;
    private final UnknownTestVisitor visitor;

    public UnknownTestTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        assertCounts = new HashMap<>();
        visitor = new UnknownTestVisitor();
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            assertCounts.put(currentMethod, 0);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        assertCounts.clear();
    }

    @Override
    public void reset(PyClass newTestCase) {
        testCase = newTestCase;
        assertCounts.clear();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", Util.mapToJsonArray(assertCounts, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return assertCounts.values().stream().anyMatch(c -> c == 0);
    }

    class UnknownTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression)) {
                return;
            }
            PyReferenceExpression pyReferenceExpression = (PyReferenceExpression) child;
            String name = pyReferenceExpression.getName();
            if (name != null && name.toLowerCase().contains("assert")) {
                assertCounts.put(currentMethod, assertCounts.get(currentMethod) + 1);
            }
        }

        public void visitPyAssertStatement(PyAssertStatement assertStatement) {
            assertCounts.put(currentMethod, assertCounts.get(currentMethod) + 1);
        }
    }
}
