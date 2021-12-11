package org.jetbrains.research.pynose.core.detectors.impl;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.*;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.*;

public class AssertionRouletteTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(AssertionRouletteTestSmellDetector.class);
    private final Map<PyFunction, List<PyCallExpression>> assertionCallsInTests = new HashMap<>();
    private final Map<PyFunction, List<PyAssertStatement>> assertStatementsInTests = new HashMap<>();
    private final Map<PyFunction, Boolean> testHasAssertionRoulette = new HashMap<>();
    private final AssertionRouletteVisitor visitor = new AssertionRouletteVisitor();

    public AssertionRouletteTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        var testMethods = PyNoseUtils.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
            currentMethod = testMethod;
            testHasAssertionRoulette.put(currentMethod, false);
            assertionCallsInTests.put(currentMethod, new ArrayList<>());
            assertStatementsInTests.put(currentMethod, new ArrayList<>());
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;

        for (var testMethod : assertionCallsInTests.keySet()) {

            var calls = assertionCallsInTests.get(testMethod);

            if (calls.size() < 2) {
                continue;
            }

            for (var call : calls) {
                PyArgumentList argumentList = call.getArgumentList();
                if (argumentList == null) {
                    LOG.warn("assertion with no argument");
                    continue;
                }
                if (argumentList.getKeywordArgument("msg") != null) {
                    continue;
                }

                if (PyNoseUtils.ASSERT_METHOD_TWO_PARAMS.contains(((PyReferenceExpression) call.getFirstChild()).getName()) &&
                        argumentList.getArguments().length < 3) {
                    testHasAssertionRoulette.replace(testMethod, true);
                } else if (PyNoseUtils.ASSERT_METHOD_ONE_PARAM.containsKey(((PyReferenceExpression) call.getFirstChild()).getName()) &&
                        argumentList.getArguments().length < 2) {
                    testHasAssertionRoulette.replace(testMethod, true);
                }
            }
        }

        for (var testMethod : assertStatementsInTests.keySet()) {
            var asserts = assertStatementsInTests.get(testMethod);
            if (asserts.size() < 2) {
                continue;
            }

            for (var assertStatement : asserts) {
                var expressions = assertStatement.getArguments();
                if (expressions.length < 2) {
                    testHasAssertionRoulette.replace(testMethod, true);
                }
            }
        }

        for (var testMethod : assertStatementsInTests.keySet()) {
            if (assertStatementsInTests.get(testMethod).size() == 1 && assertionCallsInTests.get(testMethod).size() == 1) {
                testHasAssertionRoulette.replace(testMethod, true);
            }
        }
    }

    @Override
    public void reset() {
        currentMethod = null;
        testHasAssertionRoulette.clear();
        assertionCallsInTests.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        reset();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        var detail = new JsonObject();
        detail.add("assertionCallsInTests", PyNoseUtils.mapToJsonArray(assertionCallsInTests, PyFunction::getName, Objects::toString));
        detail.add("assertStatementsInTests", PyNoseUtils.mapToJsonArray(assertStatementsInTests, PyFunction::getName, Objects::toString));
        jsonObject.add("detail", detail);
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return testHasAssertionRoulette.containsValue(true);
    }

    class AssertionRouletteVisitor extends AbstractTestSmellDetector.MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {

            var child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression) || !PyNoseUtils.isCallAssertMethod((PyReferenceExpression) child)) {
                return;
            }

            assertionCallsInTests.get(currentMethod).add(callExpression);
        }

        public void visitPyAssertStatement(PyAssertStatement assertStatement) {
            assertStatementsInTests.get(currentMethod).add(assertStatement);
        }
    }
}
