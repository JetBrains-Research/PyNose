package pytestsmelldetector;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import java.util.*;

public class GeneralFixtureTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(GeneralFixtureTestSmellDetector.class);
    private final Set<String> assignmentStatementTexts;
    private final Map<PyFunction, Set<String>> testCaseFieldsUsage;
    private final GeneralFixtureVisitor visitor;

    public GeneralFixtureTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        assignmentStatementTexts = new HashSet<>();
        testCaseFieldsUsage = new HashMap<>();
        visitor = new GeneralFixtureVisitor();
    }

    @Override
    public void analyze() {
        reset();

        Optional<PyFunction> setUpFunction = Arrays.stream(testCase.getStatementList().getStatements())
                .filter(PyFunction.class::isInstance)
                .map(PyFunction.class::cast)
                .filter(f ->
                        Objects.equals(f.getName(), "setUp") &&
                                f.getParent() instanceof PyStatementList &&
                                f.getParent().getParent() instanceof PyClass &&
                                Util.isValidUnittestCase((PyClass) f.getParent().getParent())
                )
                .findFirst();

        if (setUpFunction.isPresent()) {
            visitor.elementToCheck = PyAssignmentStatement.class;
            visitor.visitElement(setUpFunction.get());
        }

        visitor.elementToCheck = PyReferenceExpression.class;
        for (PyFunction testMethod : Util.gatherTestMethods(testCase)) {
            currentMethod = testMethod;
            testCaseFieldsUsage.put(currentMethod, new HashSet<>(assignmentStatementTexts));
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        assignmentStatementTexts.clear();
        testCaseFieldsUsage.clear();
        currentMethod = null;
    }

    @Override
    public void reset(PyClass testCase) {
        this.testCase = testCase;
        reset();
    }

    @Override
    public boolean hasSmell() {
        return testCaseFieldsUsage.values().stream().anyMatch(strings -> strings.size() > 0);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", Util.stringSetMapToJsonArray(testCaseFieldsUsage, PyFunction::getName));
        return jsonObject;
    }

    class GeneralFixtureVisitor extends MyPsiElementVisitor {
        Class<? extends PyElement> elementToCheck;

        public void visitPyAssignmentStatement(PyAssignmentStatement assignmentStatement) {
            if (!elementToCheck.equals(PyAssignmentStatement.class)) {
                for (PsiElement psiElement : assignmentStatement.getChildren()) {
                    visitElement(psiElement);
                }
                return;
            }

            for (PyExpression expression : assignmentStatement.getTargets()) {
                if (!(expression instanceof PyTargetExpression)) {
                    continue;
                }

                PyTargetExpression target = (PyTargetExpression) expression;
                if (target.getText().startsWith("self.")) {
                    assignmentStatementTexts.add(target.getText());
                }
            }
        }

        public void visitPyReferenceExpression(PyReferenceExpression referenceExpression) {
            if (!elementToCheck.equals(PyReferenceExpression.class) ||
                    !assignmentStatementTexts.contains(referenceExpression.getText())) {
                for (PsiElement psiElement : referenceExpression.getChildren()) {
                    visitElement(psiElement);
                }
                return;
            }

            testCaseFieldsUsage.get(currentMethod).remove(referenceExpression.getText());
        }
    }
}
