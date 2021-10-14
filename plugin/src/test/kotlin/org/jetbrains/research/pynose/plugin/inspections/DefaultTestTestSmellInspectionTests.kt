package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.impl.PyBuiltinCache
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseWithPythonSdkTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class DefaultTestTestSmellInspectionTests : ParametrizedBaseWithPythonSdkTest(
    getResourcesRootPath(::DefaultTestTestSmellInspectionTests)
) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun getTestData() = getInAndOutArray(
            cls = ::ParametrizedBaseWithPythonSdkTest,
            inExtension = Extension.PY,
            outExtension = null
        )
    }

    @BeforeAll
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(DefaultTestTestSmellInspection())
    }

    @Test
    fun `test SDK setup`() {
        val inPsiFile = getPsiFile(inFile!!, myFixture) as PyFile
        val builtinsCache = PyBuiltinCache.getInstance(inPsiFile)
        builtinsCache.boolType ?: error("Python SDK was not configured in the tests")
    }

    @Test
    fun `test highlighting`() {
        myFixture.configureByFile(inFile!!.path)
        myFixture.checkHighlighting()
    }

    @Test
    fun `test when no suspicious code found`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class MyTestCase():\n" +
                    "    def test_addition(self):\n" +
                    "        self.assertEquals(add(4, 5), 9)"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }
}