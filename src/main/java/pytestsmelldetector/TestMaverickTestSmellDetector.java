package pytestsmelldetector;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyTargetExpression;

import java.util.*;

public class TestMaverickTestSmellDetector extends AbstractTestSmellDetector {

    private final Map<PyFunction, Set<String>> testMethodSetUpFieldsUsage = new HashMap<>();
    private final Set<String> setUpFields = new HashSet<>();
    private final TestMaverickVisitor visitor = new TestMaverickVisitor();

    public TestMaverickTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            testMethodSetUpFieldsUsage.put(testMethod, new HashSet<>());
        }

        Optional<PyFunction> optSetUp = Arrays.stream(testCase.getStatementList().getStatements()).filter(PyFunction.class::isInstance).map(PyFunction.class::cast).filter(pyFunction -> Objects.equals(pyFunction.getName(), "setUp")).findFirst();

        if (!optSetUp.isPresent()) {
            return;
        }

        visitor.inSetUpMode = true;
        visitor.visitElement(optSetUp.get());
        visitor.inSetUpMode = false;

        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        setUpFields.clear();
        testMethodSetUpFieldsUsage.clear();
        visitor.inSetUpMode = true;
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        reset();
    }

    @Override
    public boolean hasSmell() {
        return testMethodSetUpFieldsUsage.values().stream().anyMatch(Set::isEmpty) && !setUpFields.isEmpty();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", Util.stringSetMapToJsonArray(testMethodSetUpFieldsUsage, PyFunction::getName));
        return jsonObject;
    }

    class TestMaverickVisitor extends MyPsiElementVisitor {
        boolean inSetUpMode = true;

        public void visitPyTargetExpression(PyTargetExpression targetExpression) {
            if (!inSetUpMode) {
                if (setUpFields.contains(targetExpression.getText())) {
                    testMethodSetUpFieldsUsage.get(currentMethod).add(targetExpression.getText());
                }
                return;
            }

            if (targetExpression.getText().startsWith("self.")) {
                setUpFields.add(targetExpression.getText());
            }
        }

        public void visitPyReferenceExpression(PyReferenceExpression referenceExpression) {
            if (inSetUpMode || !setUpFields.contains(referenceExpression.getText())) {
                for (PsiElement child : referenceExpression.getChildren()) {
                    visitElement(child);
                }
            } else {
                testMethodSetUpFieldsUsage.get(currentMethod).add(referenceExpression.getText());
            }
        }
    }
}
