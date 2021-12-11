package org.jetbrains.research.pynose.core.detectors.impl;

import com.google.gson.JsonObject;
import com.jetbrains.python.psi.*;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.*;

public class SuboptimalAssertTestSmellDetector extends AbstractTestSmellDetector {
    private static final List<SuboptimalAssertChecker> CHECKERS = List.of(
            SuboptimalAssertTestSmellDetector::checkAssertTrueFalseRelatedSmell,
            SuboptimalAssertTestSmellDetector::checkAssertEqualNotEqualIsIsNotRelatedSmell
    );

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

        var args = assertCall.getArguments();
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

        var args = assertCall.getArguments();
        return args.length >= 2 && Arrays.stream(args)
                .anyMatch(arg -> arg instanceof PyBoolLiteralExpression || arg instanceof PyNoneLiteralExpression);
    }

    @Override
    public void analyze() {
        var testMethods = PyNoseUtils.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
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
    public boolean hasSmell() {
        return testMethodHasSuboptimalAssert.containsValue(true);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var result = templateSmellDetailJSON();
        result.add("detail", PyNoseUtils.mapToJsonArray(testMethodHasSuboptimalAssert, PyFunction::getName, Objects::toString));
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
