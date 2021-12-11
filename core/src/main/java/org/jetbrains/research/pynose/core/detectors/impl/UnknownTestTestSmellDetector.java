package org.jetbrains.research.pynose.core.detectors.impl;


import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.*;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.HashMap;
import java.util.Objects;

public class UnknownTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(UnknownTestTestSmellDetector.class);
    private final HashMap<PyFunction, Integer> assertCounts;
    private final UnknownTestVisitor visitor;

    public UnknownTestTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        assertCounts = new HashMap<>();
        visitor = new UnknownTestVisitor();
    }

    @Override
    public void analyze() {
        var testMethods = PyNoseUtils.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
            currentMethod = testMethod;
            assertCounts.put(currentMethod, 0);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        assertCounts.clear();
    }

    @Override
    public void reset(PyClass newTestCase) {
        testCase = newTestCase;
        assertCounts.clear();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", PyNoseUtils.mapToJsonArray(assertCounts, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return assertCounts.values().stream().anyMatch(c -> c == 0);
    }

    class UnknownTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            var child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression)) {
                return;
            }
            var pyReferenceExpression = (PyReferenceExpression) child;
            var name = pyReferenceExpression.getName();
            if (name != null && name.toLowerCase().contains("assert")) {
                assertCounts.put(currentMethod, assertCounts.get(currentMethod) + 1);
            }
        }

        public void visitPyAssertStatement(PyAssertStatement assertStatement) {
            assertCounts.put(currentMethod, assertCounts.get(currentMethod) + 1);
        }
    }
}
