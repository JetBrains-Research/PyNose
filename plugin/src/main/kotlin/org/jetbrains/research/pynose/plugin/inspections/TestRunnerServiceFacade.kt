package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiFile
import com.jetbrains.python.testing.TestRunnerService

@Service
object TestRunnerServiceFacade {

    private val moduleTestRunnerMap: MutableMap<Module, String> = mutableMapOf()

    fun getConfiguredTestRunner(file: PsiFile): String? {
        val module = ModuleUtilCore.findModuleForPsiElement(file) ?: return ""
        return if (moduleTestRunnerMap.containsKey(module)) {
            moduleTestRunnerMap[module]
        } else {
            moduleTestRunnerMap[module] = TestRunnerService.getInstance(module).projectConfiguration
            moduleTestRunnerMap[module]
        }
    }
}
