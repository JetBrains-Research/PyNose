package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.openapi.components.Service
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.PyBundle
import com.jetbrains.python.psi.PyFromImportStatement
import com.jetbrains.python.psi.PyImportStatement

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

    private val testRunnerForFile: MutableMap<PsiFile, TestRunner> = mutableMapOf()

    fun getConfiguredTestRunner(file: PsiFile): TestRunner {
        if (testRunnerForFile.containsKey(file) && testRunnerForFile[file] != null) {
            return testRunnerForFile[file]!!
        }
        var hasUnittest = false
        PsiTreeUtil.findChildrenOfType(file, PyFromImportStatement::class.java).forEach { pyFromImportStatement ->
            if (pyFromImportStatement.importSource?.name?.contains("pytest") == true) {
                testRunnerForFile[file] = TestRunner.PYTEST
                return TestRunner.PYTEST
            } else if (pyFromImportStatement.importSource?.name?.contains("unittest") == true) {
                hasUnittest = true
            }
        }
        PsiTreeUtil.findChildrenOfType(file, PyImportStatement::class.java).forEach { pyImportStatement ->
            pyImportStatement.importElements.forEach { importElem ->
                if (importElem.importedQName.toString().contains("pytest")) {
                    return TestRunner.PYTEST
                } else if (importElem.importedQName.toString().contains("unittest")) {
                    hasUnittest = true
                }
            }
        }
        return if (hasUnittest) {
            testRunnerForFile[file] = TestRunner.UNITTESTS
            TestRunner.UNITTESTS
        } else {
            testRunnerForFile[file] = TestRunner.UNKNOWN
            TestRunner.UNKNOWN
        }
    }
}
