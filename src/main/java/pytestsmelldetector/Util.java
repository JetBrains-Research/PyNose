package pytestsmelldetector;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Util {
    public static boolean isValidUnittestCase(PyClass pyClass) {
        Optional<PsiElement> superClassList = Arrays.stream(pyClass.getChildren())
                .filter(PyArgumentList.class::isInstance)
                .findFirst();

        Collection<PyExpression> superClasses;
        if (!superClassList.isPresent() || (superClasses = ((PyArgumentList) superClassList.get()).getArgumentExpressions()).isEmpty())
            return false;

        for (PyExpression superClass : superClasses) {
            if (!(superClass instanceof PyReferenceExpression))
                continue;
            PyReferenceExpression superClassRef = (PyReferenceExpression) superClass;

            // TODO: do some real stuff instead of just checking variable name
            if (superClassRef.getText().equals("unittest.TestCase"))
                return true;
        }

        return false;
    }

    public static boolean isValidUnittestMethod(PyFunction pyFunction) {
        String name = pyFunction.getName();
        return name != null &&
                name.startsWith("test") &&
                pyFunction.getParent() instanceof PyStatementList &&
                pyFunction.getParent().getParent() instanceof PyClass &&
                isValidUnittestCase((PyClass) pyFunction.getParent().getParent());
    }

    public static List<PyFunction> gatherTestMethods(PyClass testCase) {
        return Arrays.stream(testCase.getStatementList().getStatements())
                .filter(PyFunction.class::isInstance)
                .map(PyFunction.class::cast)
                //.filter(Util::isValidUnittestMethod)
                .collect(Collectors.toList());
    }

    public static List<PyClass> gatherTestCases(PsiFile file) {
        return Arrays.stream(file.getChildren())
                .filter(PyClass.class::isInstance)
                .map(PyClass.class::cast)
                //.filter(Util::isValidUnittestCase)
                .collect(Collectors.toList());
    }

    public static String exceptionToString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
