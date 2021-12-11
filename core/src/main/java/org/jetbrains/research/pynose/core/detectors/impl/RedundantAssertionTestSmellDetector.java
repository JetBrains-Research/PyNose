package org.jetbrains.research.pynose.core.detectors.impl;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.*;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class RedundantAssertionTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(RedundantAssertionTestSmellDetector.class);
    private static final Set<String> OPERATOR_TEXT = Set.of("==", "!=", ">", ">=", "<=", "<", "is");
    private final HashMap<PyFunction, Integer> testMethodHaveRedundantAssertCall;
    private final HashMap<PyFunction, Integer> testMethodHaveRedundantAssertStatement;
    private final RedundantAssertionVisitor visitor = new RedundantAssertionVisitor();

    public RedundantAssertionTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodHaveRedundantAssertCall = new HashMap<>();
        testMethodHaveRedundantAssertStatement = new HashMap<>();
    }

    @Override
    public void analyze() {
        var testMethods = PyNoseUtils.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
            currentMethod = testMethod;
            testMethodHaveRedundantAssertCall.put(currentMethod, 0);
            testMethodHaveRedundantAssertStatement.put(currentMethod, 0);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        currentMethod = null;
        testMethodHaveRedundantAssertCall.clear();
        testMethodHaveRedundantAssertStatement.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        reset();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        var detail = new JsonObject();
        detail.add("testMethodHaveRedundantAssertCall", PyNoseUtils.mapToJsonArray(testMethodHaveRedundantAssertCall, PyFunction::getName, Objects::toString));
        detail.add("testMethodHaveRedundantAssertStatement", PyNoseUtils.mapToJsonArray(testMethodHaveRedundantAssertStatement, PyFunction::getName, Objects::toString));
        jsonObject.add("detail", detail);
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return testMethodHaveRedundantAssertCall.values().stream().anyMatch(c -> c > 0) ||
                testMethodHaveRedundantAssertStatement.values().stream().anyMatch(s -> s > 0);
    }

    class RedundantAssertionVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            var child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression) || !PyNoseUtils.isCallAssertMethod((PyReferenceExpression) child)) {
                return;
            }

            var argList = callExpression.getArguments(null);
            if (PyNoseUtils.ASSERT_METHOD_ONE_PARAM.containsKey(((PyReferenceExpression) child).getName())) {
                if (argList.get(0).getText().equals(PyNoseUtils.ASSERT_METHOD_ONE_PARAM.get(((PyReferenceExpression) child).getName()))) {
                    testMethodHaveRedundantAssertCall.replace(
                            currentMethod,
                            testMethodHaveRedundantAssertCall.get(currentMethod) + 1
                    );
                }
            } else if (PyNoseUtils.ASSERT_METHOD_TWO_PARAMS.contains(((PyReferenceExpression) child).getName())) {
                if (argList.get(0).getText().equals(argList.get(1).getText())) {
                    testMethodHaveRedundantAssertCall.replace(
                            currentMethod,
                            testMethodHaveRedundantAssertCall.get(currentMethod) + 1
                    );
                }
            }
        }

        public void visitPyAssertStatement(PyAssertStatement assertStatement) {
            var expressions = assertStatement.getArguments();
            if (expressions.length < 1) {
                return;
            }

            if (expressions[0] instanceof PyLiteralExpression) {
                testMethodHaveRedundantAssertStatement.replace(
                        currentMethod,
                        testMethodHaveRedundantAssertStatement.get(currentMethod) + 1
                );
                return;
            }

            if (!(expressions[0] instanceof PyBinaryExpression)) {
                return;
            }

            var binaryExpression = (PyBinaryExpression) expressions[0];
            var psiOperator = binaryExpression.getPsiOperator();
            if (psiOperator == null) {
                return;
            }

            if (binaryExpression.getChildren().length < 2) {
                return;
            }

            if (OPERATOR_TEXT.contains(psiOperator.getText()) &&
                    binaryExpression.getChildren()[0].getText().equals(binaryExpression.getChildren()[1].getText())) {
                testMethodHaveRedundantAssertStatement.replace(
                        currentMethod,
                        testMethodHaveRedundantAssertStatement.get(currentMethod) + 1
                );
            }
        }
    }
}
