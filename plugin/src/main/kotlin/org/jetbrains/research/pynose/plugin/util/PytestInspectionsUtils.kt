package org.jetbrains.research.pynose.plugin.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyStatement

object PytestInspectionsUtils {
    fun isValidPytestParent(element: PsiElement): Boolean {
        val parentFunction = PsiTreeUtil.getParentOfType(element, PyFunction::class.java) ?: return false
        return isValidPytestMethod(parentFunction)
    }

    fun isValidPytestMethod(testMethod: PyFunction): Boolean {
        val className = PsiTreeUtil.getParentOfType(testMethod, PyClass::class.java)
        return (testMethod.containingFile.name.startsWith("test") || testMethod.containingFile.name.endsWith("test.py"))
                && (testMethod.name?.startsWith("test") == true)
                && (className == null || className.name?.startsWith("Test") == true)
    }

    private fun isValidPytestMethodInsideFile(testMethod: PyFunction): Boolean {
        val className = PsiTreeUtil.getParentOfType(testMethod, PyClass::class.java)
        return (testMethod.name?.startsWith("test") == true)
                && (className == null || className.name?.startsWith("Test") == true)
    }

    fun isValidPytestFile(file: PsiFile): Boolean {
        return file.name.startsWith("test") || file.name.endsWith("test")
    }

    // TODO: implement caching?
    fun gatherValidPytestMethods(file: PyFile): List<PyFunction> {
        val returnList: MutableList<PyFunction> = mutableListOf()
        file.statements
            .filterIsInstance<PyClass>()
            .map { it as PyFunction }
            .filter { pyClass -> isValidPytestFile(pyClass.containingFile)
                    && pyClass.name?.startsWith("Test") == true }
            .forEach { pyClass ->
                pyClass.statementList.statements
                    .filterIsInstance<PyFunction>()
                    .filter { pyFunction -> isValidPytestMethodInsideFile(pyFunction) }
                    .forEach { validFunction -> returnList.add(validFunction) }
            }
        file.statements
            .filterIsInstance<PyFunction>()
            .filter { pyFunction -> isValidPytestMethodInsideFile(pyFunction) }
            .forEach { validFunction -> returnList.add(validFunction) }
        return returnList
    }

    fun gatherValidPytestClasses(file: PyFile): List<PyClass> {
        val returnList: MutableList<PyClass> = mutableListOf()
        if (!isValidPytestFile(file)) {
            return returnList
        }
        file.statements
            .filterIsInstance<PyClass>()
            .filter {pyClass -> pyClass.name?.startsWith("Test") == true }
            .map { obj: PyStatement? -> PyClass::class.java.cast(obj) }
            .filter { pyClass -> isValidPytestFile(pyClass.containingFile) }
            .forEach { pyClass -> returnList.add(pyClass) }
        return returnList
    }
}