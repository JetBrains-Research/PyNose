package org.jetbrains.research.pynose.core.detectors.impl;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyRaiseStatement;
import com.jetbrains.python.psi.PyTryExceptStatement;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExceptionHandlingTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(ExceptionHandlingTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasExceptionHandlingLogic = new HashMap<>();
    private final ExceptionHandlingVisitor visitor;

    public ExceptionHandlingTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        visitor = new ExceptionHandlingVisitor();
    }

    @Override
    public void analyze() {
        for (var testMethod : PyNoseUtils.gatherTestMethods(testCase)) {
            currentMethod = testMethod;
            testHasExceptionHandlingLogic.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testHasExceptionHandlingLogic.clear();
        currentMethod = null;
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        reset();
    }

    @Override
    public boolean hasSmell() {
        return testHasExceptionHandlingLogic.containsValue(true);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", PyNoseUtils.mapToJsonArray(testHasExceptionHandlingLogic, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    class ExceptionHandlingVisitor extends MyPsiElementVisitor {
        public void visitPyTryExceptStatement(PyTryExceptStatement tryExceptStatement) {
            testHasExceptionHandlingLogic.put(currentMethod, true);
        }

        public void visitPyRaiseStatement(PyRaiseStatement raiseStatement) {
            testHasExceptionHandlingLogic.put(currentMethod, true);
        }
    }
}
