package org.jetbrains.research.pynose.core.detectors.impl;

import com.google.gson.JsonObject;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.*;

public class TestMaverickTestSmellDetector extends AbstractTestSmellDetector {

    private final Map<PyFunction, Set<String>> testMethodSetUpFieldsUsage = new HashMap<>();
    private final Set<String> setUpFields = new HashSet<>();
    private final TestMaverickVisitor visitor = new TestMaverickVisitor();

    public TestMaverickTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
    }

    @Override
    public void analyze() {
        var testMethods = PyNoseUtils.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
            testMethodSetUpFieldsUsage.put(testMethod, new HashSet<>());
        }

        var optSetUp = Arrays.stream(testCase.getStatementList().getStatements())
                .filter(PyFunction.class::isInstance)
                .map(PyFunction.class::cast)
                .filter(pyFunction -> Objects.equals(pyFunction.getName(), "setUp"))
                .findFirst();

        if (optSetUp.isPresent()) {
            visitor.inSetUpMode = true;
            visitor.visitElement(optSetUp.get());
        }

        var optSetUpClass = Arrays.stream(testCase.getStatementList().getStatements())
                .filter(PyFunction.class::isInstance)
                .map(PyFunction.class::cast)
                .filter(pyFunction -> Objects.equals(pyFunction.getName(), "setUpClass"))
                .findFirst();

        if (optSetUpClass.isPresent()) {
            visitor.inSetUpMode = true;
            visitor.visitElement(optSetUpClass.get());
        }

        visitor.inSetUpMode = false;

        for (var testMethod : testMethods) {
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
    public boolean hasSmell() {
        return testMethodSetUpFieldsUsage.values().stream().anyMatch(Set::isEmpty) && !setUpFields.isEmpty();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", PyNoseUtils.stringSetMapToJsonArray(testMethodSetUpFieldsUsage, PyFunction::getName));
        return jsonObject;
    }

    class TestMaverickVisitor extends MyPsiElementVisitor {
        boolean inSetUpMode = true;
        String methodFirstParamName;

        public void visitPyFunction(PyFunction function) {
            if (inSetUpMode) {
                if (Objects.equals(function.getName(), "setUp") || Objects.equals(function.getName(), "setUpClass")) {
                    if (function.getParameterList().getParameters().length > 0) {
                        methodFirstParamName = function.getParameterList().getParameters()[0].getName();
                    }
                }
            }
            for (var element : function.getChildren()) {
                visitElement(element);
            }
        }

        public void visitPyTargetExpression(PyTargetExpression targetExpression) {
            if (!inSetUpMode) {
                if (setUpFields.contains(targetExpression.getText())) {
                    testMethodSetUpFieldsUsage.get(currentMethod).add(targetExpression.getText());
                }
                return;
            }

            if (targetExpression.getText().startsWith(methodFirstParamName + ".")) {
                setUpFields.add(targetExpression.getText().replace(methodFirstParamName + ".", "self."));
            }
        }

        public void visitPyReferenceExpression(PyReferenceExpression referenceExpression) {
            if (inSetUpMode || !setUpFields.contains(referenceExpression.getText())) {
                for (var child : referenceExpression.getChildren()) {
                    visitElement(child);
                }
            } else {
                testMethodSetUpFieldsUsage.get(currentMethod).add(referenceExpression.getText());
            }
        }
    }
}
