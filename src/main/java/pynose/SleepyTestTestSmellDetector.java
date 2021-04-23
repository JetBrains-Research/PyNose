package pynose;


import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiComment;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.pyi.PyiFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SleepyTestTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(SleepyTestTestSmellDetector.class);
    private final Map<PyFunction, Boolean> testHasSleepWithoutComment;
    private final SleepTestVisitor visitor;

    public SleepyTestTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        currentMethod = null;
        testHasSleepWithoutComment = new HashMap<>();
        visitor = new SleepTestVisitor();
    }

    @Override
    public void analyze() {
        for (var testMethod : Util.gatherTestMethods(testCase)) {
            currentMethod = testMethod;
            testHasSleepWithoutComment.put(currentMethod, false);
            visitor.visitElement(currentMethod);
        }
        currentMethod = null;
    }

    @Override
    public void reset() {
        currentMethod = null;
        testHasSleepWithoutComment.clear();
    }

    @Override
    public void reset(PyClass testCase) {
        this.testCase = testCase;
        reset();
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        jsonObject.add("detail", Util.mapToJsonArray(testHasSleepWithoutComment, PyFunction::getName, Objects::toString));
        return jsonObject;
    }

    @Override
    public boolean hasSmell() {
        return testHasSleepWithoutComment.containsValue(true);
    }

    class SleepTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            if (!(callExpression.getFirstChild() instanceof PyReferenceExpression)) {
                for (var child : callExpression.getChildren()) {
                    visitElement(child);
                }
                return;
            }

            var callExprRef = (PyReferenceExpression) callExpression.getFirstChild();
            var element = callExprRef.followAssignmentsChain(PyResolveContext.defaultContext()).getElement();

            if (!(element instanceof PyFunction) || !Objects.equals(((PyFunction) element).getName(), "sleep")) {
                for (var child : callExpression.getChildren()) {
                    visitElement(child);
                }
                return;
            }

            var sleep = (PyFunction) element;

            if (!(sleep.getParent() instanceof PyiFile) || !((PyiFile) sleep.getParent()).getName().equals("time.pyi")) {
                for (var child : callExpression.getChildren()) {
                    visitElement(child);
                }
                return;
            }

            // call is time.sleep
            var parent = callExpression.getParent();
            while (!(parent instanceof PyExpressionStatement)) {
                parent = parent.getParent();
            }

            if (!(parent.getLastChild() instanceof PsiComment)) {
                testHasSleepWithoutComment.replace(currentMethod, true);
            }
        }
    }
}
