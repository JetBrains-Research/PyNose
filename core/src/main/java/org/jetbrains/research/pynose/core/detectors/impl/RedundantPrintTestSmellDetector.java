package org.jetbrains.research.pynose.core.detectors.impl;

import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.pyi.PyiFile;
import org.jetbrains.research.pynose.core.PyNoseUtils;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;

import java.util.HashMap;
import java.util.Objects;

public class RedundantPrintTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(RedundantPrintTestSmellDetector.class);

    private final HashMap<PyFunction, Boolean> testMethodHavePrint;
    private final RedundantPrintTestVisitor visitor;

    public RedundantPrintTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodHavePrint = new HashMap<>();
        visitor = new RedundantPrintTestVisitor();
    }

    @Override
    public void analyze() {
        var testMethods = PyNoseUtils.gatherTestMethods(testCase);
        for (var testMethod : testMethods) {
            currentMethod = testMethod;
            testMethodHavePrint.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        testMethodHavePrint.clear();
        currentMethod = null;
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodHavePrint.clear();
        currentMethod = null;
    }

    @Override
    public boolean hasSmell() {
        return testMethodHavePrint.containsValue(true);
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", PyNoseUtils.mapToJsonArray(testMethodHavePrint, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    class RedundantPrintTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            var child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression)) {
                return;
            }

            var callMethodRef = (PyReferenceExpression) child;

            if (!callMethodRef.getText().equals("print"))
                return;

            var e = callMethodRef.followAssignmentsChain(PyResolveContext.defaultContext()).getElement();
            if (e == null)
                return;

            if (e.getParent() instanceof PyiFile &&
                    ((PyiFile) e.getParent()).getName().equals("builtins.pyi") &&
                    e.getParent().getParent() instanceof PsiDirectory &&
                    ((PsiDirectory) e.getParent().getParent()).getName().equals("stdlib")
            ) {
                testMethodHavePrint.replace(currentMethod, true);
            }
        }
    }
}
