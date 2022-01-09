package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.PyBundle
import com.jetbrains.python.psi.PyImportStatement
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
    private val testRunnerForProject: MutableMap<Project, TestRunner> = mutableMapOf()

    fun getConfiguredTestRunner(file: PsiFile): TestRunner {
        if (!moduleTestRunnerMap.containsKey(file)) {
            moduleTestRunnerMap[file] = ModuleUtilCore.findModuleForPsiElement(file) ?: return TestRunner.UNKNOWN
        }
        val selectedFactoryName = TestRunnerService.getInstance(moduleTestRunnerMap[file]).selectedFactory.name
        val runner = TestRunner.parse(selectedFactoryName)
        if (runner == TestRunner.AUTODETECT) {

            // костыль
            val project = file.project
            if (testRunnerForProject.containsKey(project)) {
                return testRunnerForProject[project]!!
            }
            testRunnerForProject[project] = TestRunner.PYTEST
            FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project))
                .filter { vFile ->
                    vFile.name.startsWith("test_") || vFile.name.endsWith("_test.py")
                }
                .map { vFile ->
                    FilenameIndex.getFilesByName(project, vFile.name, GlobalSearchScope.projectScope(project))
                }.forEach { files ->
                    files.forEach { f ->
                        if (PsiTreeUtil.findChildrenOfType(f, PyImportStatement::class.java).isNotEmpty()) {
                            testRunnerForProject[project] = TestRunner.UNITTESTS
                            return TestRunner.UNITTESTS
                        }
                    }
                }
            return testRunnerForProject[project]!!
            // костыль

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
