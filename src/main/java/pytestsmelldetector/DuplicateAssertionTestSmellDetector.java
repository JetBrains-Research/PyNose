package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;

import java.util.*;
import java.util.stream.Collectors;

public class DuplicateAssertionTestSmellDetector extends AbstractTestSmellDetector{
    private static final Logger LOG = Logger.getInstance(DuplicateAssertionTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasDuplicateAssert = new HashMap<>();
    private final Set<Set<String>> asserts = new HashSet<>();

    class DuplicateAssertionVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression) || !Util.isCallAssertMethod((PyReferenceExpression) child)) {
                return;
            }

            final Set<String> args = Arrays.stream(callExpression.getArguments())
                    .map(PsiElement::getText)
                    .collect(Collectors.toSet());

            if (asserts.contains(args)) {
                testHasDuplicateAssert.replace(currentMethod, true);
            } else {
                asserts.add(args);
            }
        }
    }

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
    public String getSmellName() {
        return "Duplicate Assertion";
    }

    @Override
    public String getSmellDetail() {
        return testHasDuplicateAssert.toString();
    }

    public Map<PyFunction, Boolean> getTestHasDuplicateAssert() {
        return testHasDuplicateAssert;
    }
}
