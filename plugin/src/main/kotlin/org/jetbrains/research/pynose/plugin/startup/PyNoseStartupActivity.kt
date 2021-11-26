package org.jetbrains.research.pynose.plugin.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyImportElement
import com.jetbrains.python.psi.PyImportStatement

// left it here in case I still need it in the future, will be deleted

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
                vFile.name.startsWith("test_") || vFile.name.endsWith("_test.py")
            }
            .map { vFile ->
                FilenameIndex.getFilesByName(project, vFile.name, GlobalSearchScope.projectScope(project))
            }

        psiFilesLists.forEach { psiFileList ->
            psiFileList.forEach { psiFile ->
                val children =
                    PsiTreeUtil.getChildrenOfType(psiFile.originalElement, PyImportStatement::class.java)
                if (children == null) {
                    pytestMode = true
                    return
                }
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
        if (!unittestMode) {
            pytestMode = true
        }
    }
}