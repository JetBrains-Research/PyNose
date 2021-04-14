package pytestsmelldetector;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class IgnoredTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(IgnoredTestTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasSkipDecorator;
    private final IgnoredTestVisitor visitor;

    public IgnoredTestTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        testHasSkipDecorator = new HashMap<>();
        currentMethod = null;
        visitor = new IgnoredTestVisitor();
    }

    @Override
    public void analyze() {
        for (PyFunction testMethod : Util.gatherTestMethods(testCase)) {
            currentMethod = testMethod;
            testHasSkipDecorator.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testHasSkipDecorator.clear();
        currentMethod = null;
    }

    @Override
    public void reset(PyClass testCase) {
        this.testCase = testCase;
        reset();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", Util.mapToJsonArray(testHasSkipDecorator, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return testHasSkipDecorator.containsValue(true);
    }

    class IgnoredTestVisitor extends MyPsiElementVisitor {
        public void visitPyDecorator(PyDecorator decorator) {
            if (!decorator.getText().startsWith("@unittest.skip")) {
                for (PsiElement element : decorator.getChildren()) {
                    visitElement(element);
                }
                return;
            }

            if (currentMethod.equals(decorator.getTarget())) {
                testHasSkipDecorator.replace(currentMethod, true);
            }
        }
    }
}
