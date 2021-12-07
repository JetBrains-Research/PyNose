package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiFile
import com.jetbrains.python.testing.TestRunnerService

@Service
object TestRunnerServiceFacade {

    private lateinit var testRunner : String

    fun getConfiguredTestRunner(): String {
        return testRunner
    }

    fun configureTestRunner(file: PsiFile): String {
        val module = ModuleUtilCore.findModuleForPsiElement(file)
        testRunner = TestRunnerService.getInstance(module).projectConfiguration
        return testRunner
    }
}
