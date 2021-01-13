package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.pyi.PyiFile;

import java.util.HashMap;
import java.util.List;

public class RedundantPrintTestSmellDetector extends AbstractTestSmellDetector {
    private static final Logger LOG = Logger.getInstance(RedundantPrintTestSmellDetector.class);

    private final HashMap<PyFunction, Boolean> testMethodHavePrint;

    class RedundantPrintTestVisitor extends MyPsiElementVisitor {
        public void visitPyCallExpression(PyCallExpression callExpression) {
            PsiElement child = callExpression.getFirstChild();
            if (!(child instanceof PyReferenceExpression)) {
                return;
            }

            PyReferenceExpression callMethodRef = (PyReferenceExpression) child;

            if (!callMethodRef.getText().equals("print"))
                return;

            PsiElement e = callMethodRef.followAssignmentsChain(PyResolveContext.defaultContext()).getElement();
            if (e == null)
                return;

            if (e.getParent() instanceof PyiFile &&
                    ((PyiFile) e.getParent()).getName().equals("builtins.pyi") &&
                    e.getParent().getParent() instanceof PsiDirectory &&
                    ((PsiDirectory) e.getParent().getParent()).getName().equals("3") &&
                    e.getParent().getParent().getParent() instanceof PsiDirectory &&
                    ((PsiDirectory) e.getParent().getParent().getParent()).getName().equals("stdlib")
            ) {
                testMethodHavePrint.replace(currentMethod, true);
            }
        }
    }

    private final RedundantPrintTestVisitor visitor;

    public RedundantPrintTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        testMethodHavePrint = new HashMap<>();
        visitor = new RedundantPrintTestVisitor();
    }

    @Override
    public void analyze() {
        List<PyFunction> testMethods = Util.gatherTestMethods(testCase);
        for (PyFunction testMethod : testMethods) {
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
    public String getSmellName() {
        return "Redundant Print";
    }

    @Override
    public String getSmellDetail() {
        return testMethodHavePrint.toString();
    }

    public HashMap<PyFunction, Boolean> getTestMethodHavePrint() {
        return testMethodHavePrint;
    }
}
