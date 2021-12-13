package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunner
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class ObscureInLineSetupTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns TestRunner.PYTEST
        myFixture.enableInspections(ObscureInLineSetupTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/obscure/pytest"
    }

    @Test
    fun `test highlighted obscure in-line setup`() {
        myFixture.configureByText(
            "test_file.py",
            "def <weak_warning descr=\"${TestSmellBundle.message("inspections.obscure.setup.description")}\">test_something</weak_warning>(self):\n" +
                    "    x1 = 1\n" +
                    "    x2 = 2\n" +
                    "    x3 = 3\n" +
                    "    x4 = 4\n" +
                    "    x5 = 5\n" +
                    "    x6 = 6\n" +
                    "    x7 = 7\n" +
                    "    x8 = 8\n" +
                    "    x9 = 9\n" +
                    "    x10 = 10\n" +
                    "    x11 = 11\n" +
                    "    assert x1 != x2"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test not more then 10 local variables in several functions`() {
        myFixture.configureByText(
            "test_file.py", "class TestClass():\n" +
                    "    def test_something(self):\n" +
                    "        x1 = 1\n" +
                    "        x2 = 2\n" +
                    "        x3 = 3\n" +
                    "        x4 = 4\n" +
                    "        x5 = 5\n" +
                    "        assert x1 != x2\n\n" +
                    "    def test_something_else(self):\n" +
                    "        x6 = 6\n" +
                    "        x7 = 7\n" +
                    "        x8 = 8\n" +
                    "        x9 = 9\n" +
                    "        x10 = 10\n" +
                    "        x11 = 11\n" +
                    "        assert x6 != x7"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test obscure in-line setup wrong class name`() {
        myFixture.configureByText(
            "test_file.py", "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        x1 = 1\n" +
                    "        x2 = 2\n" +
                    "        x3 = 3\n" +
                    "        x4 = 4\n" +
                    "        x5 = 5\n" +
                    "        x6 = 6\n" +
                    "        x7 = 7\n" +
                    "        x8 = 8\n" +
                    "        x9 = 9\n" +
                    "        x10 = 10\n" +
                    "        x11 = 11\n" +
                    "        assert x1 != x2"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WEAK_WARNING })
    }

    @Test
    fun `test obscure in-line setup multiple`() {
        myFixture.configureByFile("test_obscure_multiple.py")
        myFixture.checkHighlighting()
    }

}