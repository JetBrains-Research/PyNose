package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiFile
import com.jetbrains.python.PyBundle
import com.jetbrains.python.testing.TestRunnerService

enum class TestRunner { PYTEST, UNITTESTS, UNKNOWN }

@Service
object TestRunnerServiceFacade {

    private val moduleTestRunnerMap: MutableMap<PsiFile, Module> = mutableMapOf()

    fun getConfiguredTestRunner(file: PsiFile): TestRunner {
        if (!moduleTestRunnerMap.containsKey(file)) {
            moduleTestRunnerMap[file] = ModuleUtilCore.findModuleForPsiElement(file) ?: return TestRunner.UNKNOWN
        }
        return when (TestRunnerService.getInstance(moduleTestRunnerMap[file]).selectedFactory.name) {
            PyBundle.message("runcfg.pytest.display_name") -> return TestRunner.PYTEST
            PyBundle.message("runcfg.unittest.display_name") -> return TestRunner.UNITTESTS
            else -> TestRunner.UNKNOWN
        }
    }
}
