package pytestsmelldetector;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

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

        Optional<PyFunction> optSetUp = Arrays.stream(testCase.getStatementList().getStatements())
                .filter(PyFunction.class::isInstance)
                .map(PyFunction.class::cast)
                .filter(pyFunction -> Objects.equals(pyFunction.getName(), "setUp"))
                .findFirst();

        if (!optSetUp.isPresent()) {
            return;
        }

        visitor.inSetUpMode = true;
        visitor.visitElement(optSetUp.get());

        Optional<PyFunction> optSetUpClass = Arrays.stream(testCase.getStatementList().getStatements())
                .filter(PyFunction.class::isInstance)
                .map(PyFunction.class::cast)
                .filter(pyFunction -> Objects.equals(pyFunction.getName(), "setUpClass"))
                .findFirst();

        if (!optSetUpClass.isPresent()) {
            return;
        }
        visitor.visitElement(optSetUpClass.get());

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
        String methodFirstParamName;

        public void visitPyFunction(PyFunction function) {
            if (inSetUpMode) {
                if (Objects.equals(function.getName(), "setUp") || Objects.equals(function.getName(), "setUpClass")) {
                    if (function.getParameterList().getParameters().length > 0) {
                        methodFirstParamName = function.getParameterList().getParameters()[0].getName();
                    }
                }
            }
            for (PsiElement element : function.getChildren()) {
                visitElement(element);
            }
        }

        public void visitPyTargetExpression(PyTargetExpression targetExpression) {
            if (!inSetUpMode) {
                if (setUpFields.contains(targetExpression.getText())) {
                    testMethodSetUpFieldsUsage.get(currentMethod).add(targetExpression.getText());
                }
                return;
            }

            if (targetExpression.getText().startsWith(methodFirstParamName + ".")) {
                setUpFields.add(targetExpression.getText().replace(methodFirstParamName + ".", "self."));
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
