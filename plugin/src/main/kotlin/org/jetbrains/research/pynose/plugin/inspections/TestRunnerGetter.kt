package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiFile
import com.jetbrains.python.testing.TestRunnerService

object TestRunnerGetter {

    private var module : Module? = null

    private lateinit var testRunner : String

    fun getConfiguredTestRunner(): String {
        return testRunner
    }

    fun setModule(file: PsiFile) {
        module = ModuleUtilCore.findModuleForPsiElement(file)
    }

    fun configureTestRunner() {
        testRunner = TestRunnerService.getInstance(module).projectConfiguration
    }
}