package pytestsmelldetector;

import com.google.gson.JsonObject;
import com.jetbrains.python.psi.*;

import java.util.*;

public class SuboptimalAssertTestSmellDetector extends AbstractTestSmellDetector {
    private static final List<SuboptimalAssertChecker> CHECKERS = new ArrayList<>();

    static {
        CHECKERS.add(SuboptimalAssertTestSmellDetector::checkAssertTrueFalseRelatedSmell);
        CHECKERS.add(SuboptimalAssertTestSmellDetector::checkAssertEqualNotEqualIsIsNotRelatedSmell);
    }

    private final Map<PyFunction, Boolean> testMethodHasSuboptimalAssert = new HashMap<>();
    private final SuboptimalAssertVisitor visitor = new SuboptimalAssertVisitor();

    public SuboptimalAssertTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    public static boolean checkAssertTrueFalseRelatedSmell(PyCallExpression assertCall) {
        PyExpression callee;
        if ((callee = assertCall.getCallee()) == null) {
            return false;
        }

        if (!Objects.equals(callee.getName(), "assertTrue") && !Objects.equals(callee.getName(), "assertFalse")) {
            return false;
        }

        PyExpression[] args = assertCall.getArguments();
        return args.length >= 1 && args[0] instanceof PyBinaryExpression;
    }

    public static boolean checkAssertEqualNotEqualIsIsNotRelatedSmell(PyCallExpression assertCall) {
        PyExpression callee;
        if ((callee = assertCall.getCallee()) == null) {
            return false;
        }

        if (!Objects.equals(callee.getName(), "assertEqual") &&
                !Objects.equals(callee.getName(), "assertNotEqual") &&
                !Objects.equals(callee.getName(), "assertIs") &&
                !Objects.equals(callee.getName(), "assertIsNot")) {
            return false;
        }

        PyExpression[] args = assertCall.getArguments();
        return args.length >= 2 && ((args[1] instanceof PyBoolLiteralExpression) || (args[1] instanceof PyNoneLiteralExpression));
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
            currentMethod = testMethod;
            testMethodHasSuboptimalAssert.put(currentMethod, false);
            visitor.visitElement(testMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testMethodHasSuboptimalAssert.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        reset();
    }

    @Override
    public String getSmellName() {
        return "Suboptimal Assert";
    }

    @Override
    public boolean hasSmell() {
        return testMethodHasSuboptimalAssert.containsValue(true);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        JsonObject result = templateSmellDetailJSON();
        result.add("detail", Util.mapToJsonArray(testMethodHasSuboptimalAssert, PyFunction::getName, Objects::toString));
        return result;
    }

    interface SuboptimalAssertChecker {
        boolean check(PyCallExpression callExpression);
    }

    class SuboptimalAssertVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            testMethodHasSuboptimalAssert.replace(
                    currentMethod,
                    CHECKERS.stream().anyMatch(checker -> checker.check(callExpression)) || testMethodHasSuboptimalAssert.get(currentMethod)
            );
        }
    }
}
