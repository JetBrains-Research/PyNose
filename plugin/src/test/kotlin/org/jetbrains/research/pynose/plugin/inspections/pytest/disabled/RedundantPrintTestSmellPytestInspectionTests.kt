package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class RedundantPrintTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns "pytest"
        myFixture.enableInspections(RedundantPrintTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/redundant_print/pytest"
    }

    @Test
    fun `test highlighted redundant print`() {
        myFixture.configureByText(
            "test_file.py", "def test_something(self):\n" +
                    "    <warning descr=\"${TestSmellBundle.message("inspections.redundant.print.description")}\">print(\"hello!\")</warning>\n" +
                    "    assert 1 == 1\n" +
                    "    <warning descr=\"${TestSmellBundle.message("inspections.redundant.print.description")}\">print(\"hello again!\")</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted redundant print in class`() {
        myFixture.configureByText(
            "test_file.py", "class TestClass:\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.print.description")}\">print(\"hello!\")</warning>\n" +
                    "        assert 1 == 1\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.print.description")}\">print(\"hello again!\")</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test redundant print wrong class name`() {
        myFixture.configureByText(
            "test_file.py", "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        print(\"hello!\")"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test redundant print multiple`() {
        myFixture.configureByFile("test_redundant_print_multiple.py")
        myFixture.checkHighlighting()
    }
}