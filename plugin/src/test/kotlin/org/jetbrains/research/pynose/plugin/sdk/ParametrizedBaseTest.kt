package org.jetbrains.research.pynose.plugin.sdk

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Ignore
import java.io.File
import java.util.logging.Logger
import kotlin.reflect.KFunction

/*
 * Base class for parameterized tests that use PSI.
 * A simple example with this class can be found in the SimpleParametrizedTestTest.kt file.
 * These tests basically work according to the following pipeline:
 *  - Get several input files with the input data and output files with expected results and something like this.
 *  - For each pair run a test. If using of the output file is not necessary, you can get only input files.
 *
 * @property testDataRoot is the root of resources for the tests.
 * We should override it for each test class.
 * Please, use getResourcesRootPath function to get this path.
 */

@Ignore
open class ParametrizedBaseTest(private val testDataRoot: String = "/home/olesya/refPyNose/PyNose/plugin/src/test/resources/org/jetbrains/research/pynose/plugin") : BasePlatformTestCase() {
    protected val LOG = Logger.getLogger(javaClass.name)

    // We should define the root resources folder
    override fun getTestDataPath() = testDataRoot

    companion object {
        fun getInAndOutArray(
            cls: KFunction<ParametrizedBaseTest>,
            resourcesRootName: String = resourcesRoot,
            inExtension: Extension = Extension.KT,
            outExtension: Extension? = Extension.KT
        ): List<Array<File>> =
            getInAndOutArray(getResourcesRootPath(cls, resourcesRootName), inExtension, outExtension)

        fun getInAndOutArray(
            path: String,
            inExtension: Extension = Extension.KT,
            outExtension: Extension? = Extension.KT
        ): List<Array<File>> {
            val inAndOutFilesMap = FileTestUtil.getInAndOutDataMap(
                path,
                inFormat = TestDataFormat("in", inExtension, Type.Input),
                outFormat = outExtension?.let { TestDataFormat("out", outExtension, Type.Output) }
            )
            return inAndOutFilesMap.entries.map { (inFile, outFile) ->
                outFile?.let { arrayOf(inFile, outFile) } ?: arrayOf(inFile)
            }
        }

        // We can not get the root of the class resources automatically
        private const val resourcesRoot: String = "data"

        fun getResourcesRootPath(
            cls: KFunction<ParametrizedBaseTest>,
            resourcesRootName: String = resourcesRoot
        ): String =
            cls.javaClass.getResource(resourcesRootName)?.path ?: error("Resource ${cls.javaClass.getResource(resourcesRootName)?.path} does not exist")
    }
}
