package org.jetbrains.research.pynose.plugin.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkAdditionalData
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.projectRoots.impl.MockSdk
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.MultiMap
import com.jetbrains.python.PyNames
import com.jetbrains.python.codeInsight.typing.PyTypeShed.findRootsForLanguageLevel
import com.jetbrains.python.codeInsight.userSkeletons.PyUserSkeletonsUtil
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.sdk.PythonSdkUtil
import org.jdom.Element
import java.io.File

/*
 The copy of
 https://github.com/JetBrains/intellij-community/blob/master/python/testSrc/com/jetbrains/python/PythonMockSdk.java
* */
class PythonMockSdk(private val getTestDataPath: String) {

    fun create(name: String): Sdk {
        return create(name, LanguageLevel.getLatest())
    }

    fun create(level: LanguageLevel, vararg additionalRoots: VirtualFile?): Sdk {
        return create("MockSdk", level, *additionalRoots)
    }

    private fun create(
        name: String,
        level: LanguageLevel,
        vararg additionalRoots: VirtualFile?
    ): Sdk {
        return create(name, PyMockSdkType(level), level, *additionalRoots)
    }

    fun create(
        pathSuffix: String,
        sdkType: SdkTypeId?,
        level: LanguageLevel,
        vararg additionalRoots: VirtualFile?
    ): Sdk {
        val sdkName = "Mock " + PyNames.PYTHON_SDK_ID_NAME + " " + level.toPythonVersion()
        return create(sdkName, pathSuffix, sdkType, level, *additionalRoots)
    }

    fun create(
        name: String?,
        pathSuffix: String,
        sdkType: SdkTypeId?,
        level: LanguageLevel,
        vararg additionalRoots: VirtualFile?
    ): Sdk {
        val mockSdkPath: String = "$getTestDataPath/MockSdk/$pathSuffix"
        val roots = MultiMap.create<OrderRootType, VirtualFile>()
        roots.putValues(OrderRootType.CLASSES, createRoots(mockSdkPath, level))
        roots.putValues(OrderRootType.CLASSES, listOf(*additionalRoots))

        val sdk = MockSdk(
            name!!,
            "$mockSdkPath/bin/python",
            toVersionString(level),
            roots,
            sdkType!!
        )

        // com.jetbrains.python.psi.resolve.PythonSdkPathCache.getInstance() corrupts SDK, so have to clone
        return sdk.clone()
    }

    private fun createRoots(mockSdkPath: String, level: LanguageLevel): List<VirtualFile> {
        val result = ArrayList<VirtualFile>()
        val localFS = LocalFileSystem.getInstance()
        ContainerUtil.addIfNotNull(result, localFS.refreshAndFindFileByIoFile(File(mockSdkPath, "Lib")))
        ContainerUtil.addIfNotNull(
            result,
            localFS.refreshAndFindFileByIoFile(File(mockSdkPath, PythonSdkUtil.SKELETON_DIR_NAME))
        )
        ContainerUtil.addIfNotNull(result, PyUserSkeletonsUtil.getUserSkeletonsDirectory())
        result.addAll(findRootsForLanguageLevel(level))
        return result
    }

    private fun toVersionString(level: LanguageLevel): String {
        return "Python " + level.toPythonVersion()
    }

    inner class PyMockSdkType(private val myLevel: LanguageLevel) :
        SdkTypeId {

        override fun getName(): String {
            return PyNames.PYTHON_SDK_ID_NAME
        }

        override fun getVersionString(sdk: Sdk): String {
            return toVersionString(myLevel)
        }

        override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {}

        override fun loadAdditionalData(currentSdk: Sdk, additional: Element): SdkAdditionalData? {
            return null
        }
    }
}