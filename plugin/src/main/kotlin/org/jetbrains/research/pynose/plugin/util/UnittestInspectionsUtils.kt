package org.jetbrains.research.pynose.plugin.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.pyi.PyiFile

class UnittestInspectionsUtils : GeneralInspectionsUtils() {
    companion object {
        val ASSERT_METHOD_TWO_PARAMS = listOf(
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
        )
        val ASSERT_METHOD_ONE_PARAM = mapOf(
            Pair("assertTrue", "True"),
            Pair("assertFalse", "False"),
            Pair("assertIsNone", "None"),
            Pair("assertIsNotNone", "None")
        )

        private fun isValidUnittestCaseRecursively(pyClass: PyClass, maxDepth: Int, currentDepth: Int): Boolean {
            if (currentDepth > maxDepth) {
                return false
            }
            if (isUnittestTestCaseClass(pyClass)) {
                return true
            }
            pyClass.getSuperClasses(null)
                .forEach { superClass ->
                    if (superClass == pyClass
                        || isValidUnittestCaseRecursively(superClass, maxDepth, currentDepth + 1)
                    ) {
                        return true
                    }
                }
            return false
        }

        fun isValidUnittestCase(pyClass: PyClass): Boolean {
            return isValidUnittestCaseRecursively(pyClass, 20, 0)
        }

        fun isValidUnittestMethod(pyFunction: PyFunction?): Boolean {
            if (pyFunction == null) {
                return false
            }
            val name = pyFunction.name
            return name != null &&
                    name.startsWith("test") &&
                    pyFunction.parent is PyStatementList &&
                    pyFunction.parent.parent is PyClass &&
                    isValidUnittestCase(pyFunction.parent.parent as PyClass)
        }

        private fun isUnittestTestCaseClass(pyClass: PyClass): Boolean {
            val casePyFile = pyClass.parent
            if (casePyFile is PyiFile && casePyFile.name == "case.pyi") {
                val unittestModule = casePyFile.parent!!
                return unittestModule.name == "unittest"
            }
            return false
        }

        fun isUnittestCallAssertMethod(calledMethodRef: PyReferenceExpression): Boolean {
            if (!calledMethodRef.text.startsWith("self.assert")
                && !calledMethodRef.text.startsWith("self.fail")
            ) {
                return false
            }
            val e = calledMethodRef.followAssignmentsChain(PyResolveContext.defaultContext()).element ?: return false
            return e.parent != null &&
                    e.parent.parent is PyClass &&
                    isUnittestTestCaseClass(e.parent.parent as PyClass)
        }

        fun gatherUnittestTestMethods(testCase: PyClass): List<PyFunction> {
            return testCase.statementList.statements
                .filter { obj: PyStatement? -> PyFunction::class.java.isInstance(obj) }
                .map { obj: PyStatement? -> PyFunction::class.java.cast(obj) }
                .filter { pyFunction: PyFunction? -> isValidUnittestMethod(pyFunction) }
        }

        fun gatherUnittestTestCases(file: PsiFile): List<PyClass> {
            return file.children
                .filter { obj: PsiElement? -> PyClass::class.java.isInstance(obj) }
                .map { obj: PsiElement? -> PyClass::class.java.cast(obj) }
                .filter { pyClass: PyClass? -> isValidUnittestCase(pyClass!!) }
        }

        fun isValidUnittestParent(element: PsiElement): Boolean {
            return (isValidUnittestMethod(
                PsiTreeUtil.getParentOfType(
                    element,
                    PyFunction::class.java
                )
            )
                    )
        }

    }
}