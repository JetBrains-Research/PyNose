package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiFile
import com.jetbrains.python.PyBundle
import com.jetbrains.python.sdk.pythonSdk
import com.jetbrains.python.testing.PythonTestConfigurationType
import com.jetbrains.python.testing.TestRunnerService
import com.jetbrains.python.testing.autoDetectTests.PyAutoDetectionConfigurationFactory

enum class TestRunner {
    PYTEST, UNITTESTS, UNKNOWN, AUTODETECT;

    companion object {
        fun parse(src: String): TestRunner = when (src) {
            PyBundle.message("runcfg.pytest.display_name") -> PYTEST
            PyBundle.message("runcfg.unittest.display_name") -> UNITTESTS
            PyBundle.message("runcfg.autodetect.display_name") -> AUTODETECT
            else -> UNKNOWN
        }
    }
}

@Service
object TestRunnerServiceFacade {

    private val moduleTestRunnerMap: MutableMap<PsiFile, Module> = mutableMapOf()

    fun getConfiguredTestRunner(file: PsiFile): TestRunner {
        if (!moduleTestRunnerMap.containsKey(file)) {
            moduleTestRunnerMap[file] = ModuleUtilCore.findModuleForPsiElement(file) ?: return TestRunner.UNKNOWN
        }
        val selectedFactoryName = TestRunnerService.getInstance(moduleTestRunnerMap[file]).selectedFactory.name
        val runner = TestRunner.parse(selectedFactoryName)
        if (runner == TestRunner.AUTODETECT) {
            val sdk = file.project.pythonSdk ?: return TestRunner.UNKNOWN
            // Retrieving the first installed test factory
            for (factory in PythonTestConfigurationType.getInstance().typedFactories) {
                if (factory.isFrameworkInstalled(sdk) && factory !is PyAutoDetectionConfigurationFactory) {
                    return TestRunner.parse(factory.name)
                }
            }
            return TestRunner.UNKNOWN
        } else {
            return runner
        }
    }
}
