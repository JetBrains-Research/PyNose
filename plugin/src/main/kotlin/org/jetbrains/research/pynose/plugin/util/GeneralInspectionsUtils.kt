package org.jetbrains.research.pynose.plugin.util

import com.google.gson.JsonArray
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.testing.TestRunnerService
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import java.util.*

object GeneralInspectionsUtils {
    private val LOG = Logger.getInstance(GeneralInspectionsUtils::class.java)

    private var unittestMode = false
    private var pytestMode = false

    fun getPluginUnittestMode(): Boolean {
        return unittestMode
    }

    fun getPluginPytestMode(): Boolean {
        return pytestMode
    }

    // can be checked for either a psi element or a file
    fun configureMode(element: PsiElement) {
        val module: Module? = ModuleUtilCore.findModuleForPsiElement(element)
        val testRunner = TestRunnerService.getInstance(module)
        when (testRunner.projectConfiguration) {
            "Unittests" -> {
                unittestMode = true
                pytestMode = false
            }
            "pytest" -> {
                unittestMode = false
                pytestMode = true
            }
            else -> {
                TODO("Not yet implemented (autodetect)")
            }
        }
    }

    fun checkValidParent(element: PsiElement): Boolean {
        if (PyNoseMode.getPyNosePytestMode()) {
            return PytestInspectionsUtils.isValidPytestParent(element)
        } else if (PyNoseMode.getPyNoseUnittestMode()) {
            return UnittestInspectionsUtils.isValidUnittestParent(element)
        }
        return false
    }

    fun checkValidMethod(testMethod: PyFunction): Boolean {
        if (PyNoseMode.getPyNosePytestMode()) {
            return PytestInspectionsUtils.isValidPytestMethod(testMethod)
        } else if (PyNoseMode.getPyNoseUnittestMode()) {
            return UnittestInspectionsUtils.isValidUnittestMethod(testMethod)
        }
        return false
    }

    fun extractPsiFromProject(project: Project?): List<PsiFile> {
        val projectPsiFiles: MutableList<PsiFile> = ArrayList()
        ProjectRootManager.getInstance(project!!).contentRoots
            .filter { obj: VirtualFile? -> Objects.nonNull(obj) }
            .forEach { root: VirtualFile? ->
                VfsUtilCore.iterateChildrenRecursively(root!!, null) { virtualFile: VirtualFile ->
                    if (virtualFile.extension == "py" && virtualFile.canonicalPath != null) {
                        val psi = PsiManager.getInstance(project).findFile(virtualFile)
                        if (psi != null) {
                            projectPsiFiles.add(psi)
                        }
                    }
                    true
                }
            }
        return projectPsiFiles
    }

    fun <K, V> mapToJsonArray(map: Map<K, V>, kSerializer: Serializer<K>, vSerializer: Serializer<V>): JsonArray {
        val mapArray = JsonArray()
        map.forEach { (k: K, v: V) ->
            val mapEntry = JsonArray()
            mapEntry.add(kSerializer.serialize(k))
            mapEntry.add(vSerializer.serialize(v))
            mapArray.add(mapEntry)
        }
        return mapArray
    }

    fun <K> stringSetMapToJsonArray(map: Map<K, Set<String?>>, kSerializer: Serializer<K>): JsonArray {
        val mapArray = JsonArray()
        map.forEach { (k: K, vSet: Set<String?>) ->
            val mapEntry = JsonArray()
            mapEntry.add(kSerializer.serialize(k))
            val setArray = JsonArray()
            vSet.forEach { string: String? -> setArray.add(string) }
            mapEntry.add(setArray)
            mapArray.add(mapEntry)
        }
        return mapArray
    }

    fun exceptionToString(ex: Exception): String {
        return ex.printStackTrace().toString()
    }

}

interface Serializer<T> {
    fun serialize(t: T): String?
}
