package org.jetbrains.research.pynose.core.detectors.impl;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.python.psi.*;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.*;

public class GeneralFixtureTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(GeneralFixtureTestSmellDetector.class);
    private final Set<String> assignmentStatementTexts;
    private final Map<PyFunction, Set<String>> testCaseFieldsUsage;
    private final GeneralFixtureVisitor visitor;

    public GeneralFixtureTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        assignmentStatementTexts = new HashSet<>();
        testCaseFieldsUsage = new HashMap<>();
        visitor = new GeneralFixtureVisitor();
    }

    @Override
    public void analyze() {
        reset();

        var setUpFunction = Arrays.stream(testCase.getStatementList().getStatements())
                .filter(PyFunction.class::isInstance)
                .map(PyFunction.class::cast)
                .filter(f ->
                        Objects.equals(f.getName(), "setUp") &&
                                f.getParent() instanceof PyStatementList &&
                                f.getParent().getParent() instanceof PyClass &&
                                PyNoseUtils.isValidUnittestCase((PyClass) f.getParent().getParent())
                )
                .findFirst();

        if (setUpFunction.isPresent()) {
            visitor.elementToCheck = PyAssignmentStatement.class;
            visitor.visitElement(setUpFunction.get());
        }

        var setUpClassFunction = Arrays.stream(testCase.getStatementList().getStatements())
                .filter(PyFunction.class::isInstance)
                .map(PyFunction.class::cast)
                .filter(f ->
                        Objects.equals(f.getName(), "setUpClass") &&
                                f.getParent() instanceof PyStatementList &&
                                f.getParent().getParent() instanceof PyClass &&
                                PyNoseUtils.isValidUnittestCase((PyClass) f.getParent().getParent())
                )
                .findFirst();

        if (setUpClassFunction.isPresent()) {
            visitor.elementToCheck = PyAssignmentStatement.class;
            visitor.visitElement(setUpClassFunction.get());
        }

        visitor.elementToCheck = PyReferenceExpression.class;
        for (var testMethod : PyNoseUtils.gatherTestMethods(testCase)) {
            currentMethod = testMethod;
            testCaseFieldsUsage.put(currentMethod, new HashSet<>(assignmentStatementTexts));
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        assignmentStatementTexts.clear();
        testCaseFieldsUsage.clear();
        currentMethod = null;
    }

    @Override
    public void reset(PyClass testCase) {
        this.testCase = testCase;
        reset();
    }

    @Override
    public boolean hasSmell() {
        return testCaseFieldsUsage.values().stream().anyMatch(strings -> strings.size() > 0);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", PyNoseUtils.stringSetMapToJsonArray(testCaseFieldsUsage, PyFunction::getName));
        return jsonObject;
    }

    class GeneralFixtureVisitor extends MyPsiElementVisitor {
        Class<? extends PyElement> elementToCheck;
        String methodFirstParamName;

        public void visitPyFunction(PyFunction function) {
            if (elementToCheck.equals(PyAssignmentStatement.class)) {
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

        public void visitPyAssignmentStatement(PyAssignmentStatement assignmentStatement) {
            if (!elementToCheck.equals(PyAssignmentStatement.class)) {
                for (var psiElement : assignmentStatement.getChildren()) {
                    visitElement(psiElement);
                }
                return;
            }

            for (var expression : assignmentStatement.getTargets()) {
                if (!(expression instanceof PyTargetExpression)) {
                    continue;
                }

                var target = (PyTargetExpression) expression;
                if (target.getText().startsWith(methodFirstParamName + ".")) {
                    assignmentStatementTexts.add(target.getText().replace(methodFirstParamName + ".", "self."));
                }
            }
        }

        public void visitPyReferenceExpression(PyReferenceExpression referenceExpression) {
            if (!elementToCheck.equals(PyReferenceExpression.class) ||
                    !assignmentStatementTexts.contains(referenceExpression.getText())) {
                for (var psiElement : referenceExpression.getChildren()) {
                    visitElement(psiElement);
                }
                return;
            }

            testCaseFieldsUsage.get(currentMethod).remove(referenceExpression.getText());
        }
    }
}
