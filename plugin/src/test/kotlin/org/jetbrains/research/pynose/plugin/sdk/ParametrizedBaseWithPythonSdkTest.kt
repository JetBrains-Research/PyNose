package org.jetbrains.research.pynose.plugin.sdk


import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ProjectRootManager
import org.junit.Ignore

/*
 * Base class for parameterized tests with Python SDK.
 * Sometimes we need to use some information that is available only if SDK connected,
 * for example, some information from PyBuiltinCache.
 */
@Ignore
open class ParametrizedBaseWithPythonSdkTest(testDataRoot: String) : ParametrizedBaseTest(testDataRoot) {
    private lateinit var sdk: Sdk

    override fun setUp() {
        super.setUp()
        setupSdk()
        println("sdk setup finished")
    }

    override fun tearDown() {
        ApplicationManager.getApplication().runWriteAction {
            ProjectJdkTable.getInstance().removeJdk(sdk)
        }
        super.tearDown()
    }

    private fun setupSdk() {
        val project = myFixture.project
        val projectManager = ProjectRootManager.getInstance(project)
        sdk = PythonMockSdk(testDataPath).create("3.8")
        val sdkConfigurer = SdkConfigurer(project, projectManager)
        sdkConfigurer.setProjectSdk(sdk)
    }
}