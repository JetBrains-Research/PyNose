package org.jetbrains.research.pynose.core.detectors.impl;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.*;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MagicNumberTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(MagicNumberTestTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testMethodHasMagicNumber;
    private final MagicNumberTestVisitor visitor;

    public MagicNumberTestTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodHasMagicNumber = new HashMap<>();
        visitor = new MagicNumberTestVisitor();
        currentMethod = null;
    }

    @Override
    public void analyze() {
        var testMethods = PyNoseUtils.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
            currentMethod = testMethod;
            testMethodHasMagicNumber.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testMethodHasMagicNumber.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodHasMagicNumber.clear();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", PyNoseUtils.mapToJsonArray(testMethodHasMagicNumber, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return testMethodHasMagicNumber.containsValue(true);
    }

    class MagicNumberTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            var child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression) || !PyNoseUtils.isCallAssertMethod((PyReferenceExpression) child)) {
                return;
            }

            if (Arrays.stream(callExpression.getArguments()).anyMatch(PyNumericLiteralExpression.class::isInstance)) {
                testMethodHasMagicNumber.put(currentMethod, true);
            }
        }
    }
}
