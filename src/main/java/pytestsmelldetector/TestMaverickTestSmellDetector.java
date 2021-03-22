package pytestsmelldetector;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyTargetExpression;

import java.util.*;

public class TestMaverickTestSmellDetector extends AbstractTestSmellDetector {

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
            if (inSetUpMode) {
                for (PsiElement child : referenceExpression.getChildren()) {
                    visitElement(child);
                }
                return;
            }

            if (setUpFields.contains(referenceExpression.getText())) {
                testMethodSetUpFieldsUsage.get(currentMethod).add(referenceExpression.getText());
            }
        }
    }

    private final Map<PyFunction, Set<String>> testMethodSetUpFieldsUsage = new HashMap<>();
    private final Set<String> setUpFields = new HashSet<>();
    private final TestMaverickVisitor visitor = new TestMaverickVisitor();

    public TestMaverickTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for(PyFunction testMethod : testMethods) {
            testMethodSetUpFieldsUsage.put(testMethod, new HashSet<>());
        }

        Optional<PyFunction> optSetUp = Arrays.stream(testCase.getStatementList().getStatements()).filter(PyFunction.class::isInstance).map(PyFunction.class::cast).filter(pyFunction -> Objects.equals(pyFunction.getName(), "setUp")).findFirst();

        if (!optSetUp.isPresent()) {
            return;
        }

        visitor.inSetUpMode = true;
        visitor.visitElement(optSetUp.get());
        visitor.inSetUpMode = false;

        for(PyFunction testMethod : testMethods) {
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
    public String getSmellName() {
        return "TestMaverickTestSmellDetector";
    }

    @Override
    public String getSmellDetail() {
        return testMethodSetUpFieldsUsage.toString();
    }
}
