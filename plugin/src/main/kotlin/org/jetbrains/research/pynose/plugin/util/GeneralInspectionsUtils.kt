package org.jetbrains.research.pynose.plugin.util

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.TestRunner
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade

object GeneralInspectionsUtils {
    private val LOG = Logger.getInstance(GeneralInspectionsUtils::class.java)

    fun checkValidParent(element: PsiElement): Boolean {
        if (element.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(element.containingFile) == TestRunner.PYTEST) {
            return PytestInspectionsUtils.isValidPytestParent(element)
        } else if (element.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(element.containingFile) == TestRunner.UNITTESTS) {
            return UnittestInspectionsUtils.isValidUnittestParent(element)
        }
        return false
    }

    fun checkValidMethod(testMethod: PyFunction): Boolean {
        if (testMethod.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(testMethod.containingFile) == TestRunner.PYTEST) {
            return PytestInspectionsUtils.isValidPytestMethod(testMethod)
        } else if (testMethod.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(testMethod.containingFile) == TestRunner.UNITTESTS) {
            return UnittestInspectionsUtils.isValidUnittestMethod(testMethod)
        }
        return false
    }
}
