package org.jetbrains.research.pynose.plugin.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyImportElement
import com.jetbrains.python.psi.PyImportStatement

class PyNoseStartupActivity : StartupActivity {
    companion object {
        private var unittestMode = false
        private var pytestMode = false

        fun getPluginUnittestMode(): Boolean {
            return unittestMode
        }

        fun getPluginPytestMode(): Boolean {
            return pytestMode
        }
    }

    override fun runActivity(project: Project) {
        val psiFilesLists = FilenameIndex.getAllFilesByExt(project, "py", GlobalSearchScope.projectScope(project))
            .filter { vFile ->
                vFile.name.startsWith("test_") || vFile.name.endsWith("_test.py") // todo correct check
            }
            .map { vFile ->
                FilenameIndex.getFilesByName(project, vFile.name, GlobalSearchScope.projectScope(project))
            }

        psiFilesLists.forEach { psiFileList ->
            psiFileList.forEach { psiFile ->
                val children =
                    PsiTreeUtil.getChildrenOfType(psiFile.originalElement, PyImportStatement::class.java) ?: return
                children
                    .forEach { child ->
                        PsiTreeUtil.getChildrenOfType(child, PyImportElement::class.java)
                            .forEach { importElement ->
                                if (importElement.importedQName.toString() == "unittest") {
                                    unittestMode = true
                                }
                            }
                    }
            }
        }

        // todo impl pytest check

//        fun enableSingleInspection(inspectionName: String) =
//            project.enableSingleInspection(inspectionName)
//
//        enableSingleInspection("ConditionalTestLogicTestSmellInspection") // bundle?
    }
}