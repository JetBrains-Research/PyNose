package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStatementList;
import com.jetbrains.python.pyi.PyiFile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    private static final Logger LOG = Logger.getInstance(Util.class);

    public static boolean isValidUnittestCase(PyClass pyClass) {
        PyClass[] superClasses = pyClass.getSuperClasses(null);
        for (PyClass c : superClasses) {
            PsiElement casePyFile = c.getParent();
            if (casePyFile instanceof PyiFile && ((PyiFile) casePyFile).getName().equals("case.pyi")) {  // TODO: still hardcoding?
                PsiElement unittestModule = casePyFile.getParent();
                if (unittestModule instanceof PsiDirectory && ((PsiDirectory) unittestModule).getName().equals("unittest")) {
                    return true;
                }
            }
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
                .filter(Util::isValidUnittestMethod)
                .collect(Collectors.toList());
    }

    public static List<PyClass> gatherTestCases(PsiFile file) {
        return Arrays.stream(file.getChildren())
                .filter(PyClass.class::isInstance)
                .map(PyClass.class::cast)
                .filter(Util::isValidUnittestCase)
                .collect(Collectors.toList());
    }

    public static String exceptionToString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
