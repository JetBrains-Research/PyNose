package org.jetbrains.research.pynose.plugin.sdk

import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.impl.PyBuiltinCache
import org.jetbrains.research.pynose.plugin.inspections.DefaultTestTestSmellInspection
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class TestDefaultTestTestSmellInspection :
    ParametrizedBaseWithPythonSdkTest(getResourcesRootPath(::TestDefaultTestTestSmellInspection)) {

    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = getInAndOutArray(
            ::ParametrizedBaseWithPythonSdkTest,
            inExtension = Extension.PY,
            outExtension = Extension.PY
        )
    }

    @Test
    fun testDefaultTestCaseNameUnittestSdk() {
//        myFixture.configureByFile(inFile!!.path)
        getPsiFile(inFile!!, myFixture)
        myFixture.enableInspections(DefaultTestTestSmellInspection())
        myFixture.checkHighlighting()
    }

    // copied from plugin-utilities
    @Test
    fun checkSDKTest() {
        val inPsiFile = getPsiFile(inFile!!, myFixture) as PyFile
        val builtinsCache = PyBuiltinCache.getInstance(inPsiFile)
        builtinsCache.boolType ?: error("Python SDK was not configured in the tests")
    }

    @Test
    fun testDefaultTestCaseNameUnittestText() {
        myFixture.configureByText(
            "file.py", "class MyTestCase(unittest.TestCase):\n" +
                    "    def test_add1(self):\n" +
                    "        self.assertEquals(add(4, 5), 9)\n"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun testNotDefaultTestCase() {
        myFixture.configureByText(
            "file.py", "class MyTestCase(unittest.TestCase):\n" +
                    "    def test_addition(self):\n" +
                    "        self.assertEquals(add(4, 5), 9)"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun testDefaultTestCaseNameNotUnittest() {
        myFixture.configureByText(
            "file.py", "class MyTestCase():\n" +
                    "    def test_addition(self):\n" +
                    "        self.assertEquals(add(4, 5), 9)"
        )
        myFixture.checkHighlighting()
    }

}