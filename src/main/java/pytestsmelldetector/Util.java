package pytestsmelldetector;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyStatementList;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.pyi.PyiFile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Util {
    public final static List<String> ASSERT_METHOD_TWO_PARAMS = Arrays.asList(
            "assertEqual",
            "assertNotEqual",
            "assertIs",
            "assertIsNot",
            "assertAlmostEqual",
            "assertNotAlmostEqual",
            "assertGreater",
            "assertGreaterEqual",
            "assertLess",
            "assertLessEqual",
            "assertCountEqual",
            "assertMultiLineEqual",
            "assertSequenceEqual",
            "assertListEqual",
            "assertTupleEqual",
            "assertSetEqual",
            "assertDictEqual"
    );
    public final static Map<String, String> ASSERT_METHOD_ONE_PARAM = new HashMap<>(4);
    private static final Logger LOG = Logger.getInstance(Util.class);

    static {
        ASSERT_METHOD_ONE_PARAM.put("assertTrue", "True");
        ASSERT_METHOD_ONE_PARAM.put("assertFalse", "False");
        ASSERT_METHOD_ONE_PARAM.put("assertIsNone", "None");
        ASSERT_METHOD_ONE_PARAM.put("assertIsNotNone", "None");
    }

    public static List<PsiFile> extractPsiFromProject(Project project) {
        List<PsiFile> projectPsiFiles = new ArrayList<>();
        Arrays.stream(ProjectRootManager.getInstance(project).getContentRoots())
                .filter(Objects::nonNull)
                .forEach(root -> VfsUtilCore.iterateChildrenRecursively(root, null, virtualFile -> {
                    if (Objects.equals(virtualFile.getExtension(), "py") && virtualFile.getCanonicalPath() != null) {
                        PsiFile psi = PsiManager.getInstance(project).findFile(virtualFile);
                        if (psi != null) {
                            projectPsiFiles.add(psi);
                        }
                    }
                    return true;
                }));

        return projectPsiFiles;
    }

    private static boolean isValidUnittestCaseRecursively(PyClass pyClass, int maxRecursionDepth, int currentRecursionDepth) {
        if (currentRecursionDepth > maxRecursionDepth) {
            return false;
        }

        PyClass[] superClasses = pyClass.getSuperClasses(null);
        for (PyClass c : superClasses) {
            if (isTestCaseClass(c)) {
                return true;
            }

            for (PyClass cSuper : c.getSuperClasses(null)) {
                if (isValidUnittestCaseRecursively(cSuper, maxRecursionDepth, currentRecursionDepth + 1)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isValidUnittestCase(PyClass pyClass) {
        return isValidUnittestCaseRecursively(pyClass, Integer.MAX_VALUE, 0);
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

    public static boolean isTestCaseClass(PyClass pyClass) {
        PsiElement casePyFile = pyClass.getParent();
        if (casePyFile instanceof PyiFile && ((PyiFile) casePyFile).getName().equals("case.pyi")) {
            PsiElement unittestModule = casePyFile.getParent();
            return unittestModule instanceof PsiDirectory && ((PsiDirectory) unittestModule).getName().equals("unittest");
        }
        return false;
    }

    public static boolean isCallAssertMethod(PyReferenceExpression calledMethodRef) {
        if (!calledMethodRef.getText().startsWith("self.assert") && !calledMethodRef.getText().startsWith("self.fail"))
            return false;

        PsiElement e = calledMethodRef.followAssignmentsChain(PyResolveContext.defaultContext()).getElement();
        if (e == null)
            return false;

        return e.getParent() != null &&
                e.getParent().getParent() instanceof PyClass &&
                isTestCaseClass((PyClass) e.getParent().getParent());
    }

    public static List<AbstractTestSmellDetector> newAllDetectors(PyClass testCase) {
        return Arrays.asList(
                new AssertionRouletteTestSmellDetector(testCase),
                new ConditionalTestLogicTestSmellDetector(testCase),
                new ConstructorInitializationTestSmellDetector(testCase),
                new DefaultTestTestSmellDetector(testCase),
                new DuplicateAssertionTestSmellDetector(testCase),
                new EagerTestTestSmellDetector(testCase),
                new EmptyTestTestSmellDetector(testCase),
                new ExceptionHandlingTestSmellDetector(testCase),
                new GeneralFixtureTestSmellDetector(testCase),
                new IgnoredTestTestSmellDetector(testCase),
                new MagicNumberTestTestSmellDetector(testCase),
                new RedundantAssertionTestSmellDetector(testCase),
                new RedundantPrintTestSmellDetector(testCase),
                new SleepyTestTestSmellDetector(testCase),
                new UnknownTestTestSmellDetector(testCase),
                new ObscureInLineSetupTestSmellDetector(testCase),
                new TestMaverickTestSmellDetector(testCase),
                new LackCohesionTestSmellDetector(testCase)
        );
    }
}
