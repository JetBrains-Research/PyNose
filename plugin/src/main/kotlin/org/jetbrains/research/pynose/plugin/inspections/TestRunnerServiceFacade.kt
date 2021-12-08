package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiFile
import com.jetbrains.python.testing.TestRunnerService

@Service
object TestRunnerServiceFacade {

    private val moduleTestRunnerMap: MutableMap<PsiFile, Module> = mutableMapOf()

    fun getConfiguredTestRunner(file: PsiFile): String {
        if (!moduleTestRunnerMap.containsKey(file)) {
            moduleTestRunnerMap[file] = ModuleUtilCore.findModuleForPsiElement(file) ?: return ""
        }
        return TestRunnerService.getInstance(moduleTestRunnerMap[file]).projectConfiguration
    }
}
