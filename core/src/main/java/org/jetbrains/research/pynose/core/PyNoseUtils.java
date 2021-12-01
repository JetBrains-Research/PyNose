package org.jetbrains.research.pynose.core;

import com.google.gson.JsonArray;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyStatementList;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.pyi.PyiFile;
import org.jetbrains.research.pynose.core.detectors.AbstractTestSmellDetector;
import org.jetbrains.research.pynose.core.detectors.impl.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class PyNoseUtils {
    public final static List<String> ASSERT_METHOD_TWO_PARAMS = List.of(
            "assertEqual",
            "assertNotEqual",
            "assertIs",
            "assertIsNot",
            "assertIn",
            "assertNotIn",
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
    public final static Map<String, String> ASSERT_METHOD_ONE_PARAM = Map.of(
            "assertTrue", "True",
            "assertFalse", "False",
            "assertIsNone", "None",
            "assertIsNotNone", "None"
    );
    private static final Logger LOG = Logger.getInstance(PyNoseUtils.class);

    public static List<PsiFile> extractPsiFromProject(Project project) {
        List<PsiFile> projectPsiFiles = new ArrayList<>();
        Arrays.stream(ProjectRootManager.getInstance(project).getContentRoots())
                .filter(Objects::nonNull)
                .forEach(root -> VfsUtilCore.iterateChildrenRecursively(root, null, virtualFile -> {
                    if (Objects.equals(virtualFile.getExtension(), "py") && virtualFile.getCanonicalPath() != null) {
                        var psi = PsiManager.getInstance(project).findFile(virtualFile);
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

        if (isTestCaseClass(pyClass)) {
            return true;
        }

        var superClasses = pyClass.getSuperClasses(null);
        for (var superClass : superClasses) {
            if (superClass.equals(pyClass) || isValidUnittestCaseRecursively(superClass, maxRecursionDepth, currentRecursionDepth + 1)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidUnittestCase(PyClass pyClass) {
        return isValidUnittestCaseRecursively(pyClass, 20, 0);
    }

    public static boolean isValidUnittestMethod(PyFunction pyFunction) {
        if (pyFunction == null) {
            return false;
        }
        var name = pyFunction.getName();
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
                .filter(PyNoseUtils::isValidUnittestMethod)
                .collect(Collectors.toList());
    }

    public static List<PyClass> gatherTestCases(PsiFile file) {
        return Arrays.stream(file.getChildren())
                .filter(PyClass.class::isInstance)
                .map(PyClass.class::cast)
                .filter(PyNoseUtils::isValidUnittestCase)
                .collect(Collectors.toList());
    }

    public static String exceptionToString(Exception e) {
        var stringWriter = new StringWriter();
        var printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public static boolean isTestCaseClass(PyClass pyClass) {
        var casePyFile = pyClass.getParent();
        if (casePyFile instanceof PyiFile && ((PyiFile) casePyFile).getName().equals("case.pyi")) {
            var unittestModule = casePyFile.getParent();
            return unittestModule instanceof PsiDirectory && ((PsiDirectory) unittestModule).getName().equals("unittest");
        }
        return false;
    }

    public static boolean isCallAssertMethod(PyReferenceExpression calledMethodRef) {
        if (!calledMethodRef.getText().startsWith("self.assert") && !calledMethodRef.getText().startsWith("self.fail"))
            return false;

        var e = calledMethodRef.followAssignmentsChain(PyResolveContext.defaultContext()).getElement();
        if (e == null)
            return false;

        return e.getParent() != null &&
                e.getParent().getParent() instanceof PyClass &&
                isTestCaseClass((PyClass) e.getParent().getParent());
    }

    public static List<AbstractTestSmellDetector> newAllDetectors(PyClass testCase) {
        return List.of(
                new AssertionRouletteTestSmellDetector(testCase),
                new ConditionalTestLogicTestSmellDetector(testCase),
                new ConstructorInitializationTestSmellDetector(testCase),
                new DefaultTestTestSmellDetector(testCase),
                new DuplicateAssertionTestSmellDetector(testCase),
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
                new LackCohesionTestSmellDetector(testCase),
                new SuboptimalAssertTestSmellDetector(testCase)
        );
    }

    public static <K, V> JsonArray mapToJsonArray(Map<K, V> map, Serializer<K> kSerializer, Serializer<V> vSerializer) {
        var mapArray = new JsonArray();
        map.forEach((k, v) -> {
            var mapEntry = new JsonArray();
            mapEntry.add(kSerializer.serialize(k));
            mapEntry.add(vSerializer.serialize(v));
            mapArray.add(mapEntry);
        });
        return mapArray;
    }

    public static <K> JsonArray stringSetMapToJsonArray(Map<K, Set<String>> map, Serializer<K> kSerializer) {
        var mapArray = new JsonArray();
        map.forEach((k, vSet) -> {
            var mapEntry = new JsonArray();
            mapEntry.add(kSerializer.serialize(k));
            var setArray = new JsonArray();
            vSet.forEach(setArray::add);
            mapEntry.add(setArray);
            mapArray.add(mapEntry);
        });
        return mapArray;
    }

    public interface Serializer<T> {
        String serialize(T t);
    }
}
