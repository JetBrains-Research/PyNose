package org.jetbrains.research.pynose.core.detectors.impl;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.*;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.*;

public class DuplicateAssertionTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(DuplicateAssertionTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasDuplicateAssertCall = new HashMap<>();
    private final Map<PyFunction, Boolean> testHasDuplicateAssertStatement = new HashMap<>();
    private final Set<String> assertCalls = new HashSet<>();
    private final Set<String> assertStatements = new HashSet<>();
    private final DuplicateAssertionVisitor visitor = new DuplicateAssertionVisitor();

    public DuplicateAssertionTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        var testMethods = PyNoseUtils.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
            currentMethod = testMethod;
            assertCalls.clear();
            assertStatements.clear();
            testHasDuplicateAssertCall.put(currentMethod, false);
            testHasDuplicateAssertStatement.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testHasDuplicateAssertCall.clear();
        testHasDuplicateAssertStatement.clear();
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
        detail.add("testHasDuplicateAssertCall", PyNoseUtils.mapToJsonArray(testHasDuplicateAssertCall, PyFunction::getName, Objects::toString));
        detail.add("testHasDuplicateAssertStatement", PyNoseUtils.mapToJsonArray(testHasDuplicateAssertStatement, PyFunction::getName, Objects::toString));
        jsonObject.add("detail", detail);
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return testHasDuplicateAssertCall.containsValue(true) || testHasDuplicateAssertStatement.containsValue(true);
    }

    class DuplicateAssertionVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            var child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression) || !PyNoseUtils.isCallAssertMethod((PyReferenceExpression) child)) {
                return;
            }

            var assertionCall = callExpression.getText();
            if (assertCalls.contains(assertionCall)) {
                testHasDuplicateAssertCall.replace(currentMethod, true);
            } else {
                assertCalls.add(assertionCall);
            }
        }

        public void visitPyAssertStatement(PyAssertStatement assertStatement) {
            var assertArgs = assertStatement.getArguments();
            if (assertArgs.length < 1) {
                return;
            }

            String assertStatementBody = assertArgs[0].getText();
            if (assertStatements.contains(assertStatementBody)) {
                testHasDuplicateAssertStatement.replace(currentMethod, true);
            } else {
                assertStatements.add(assertStatementBody);
            }
        }
    }
}
