package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class MagicNumberTestTestSmellPytestInspectionTests: AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every {
            myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any())
        } returns "pytest"
        myFixture.enableInspections(MagicNumberTestTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/magic_number/pytest"
    }

    @Test
    fun `test highlighted magic number`() {
        myFixture.configureByText(
            "test_file.py", "class TestClass:\n" +
                    "    def test_something(self):\n" +
                    "        <weak_warning descr=\"${TestSmellBundle.message("inspections.magic.number.description")}\">assert 1 == 1</weak_warning>\n" +
                    "        assert \"H\" != \"F\"\n" +
                    "        <weak_warning descr=\"${TestSmellBundle.message("inspections.magic.number.description")}\">assert 2 == 1 + 2</weak_warning>\n"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted combination of numbers and letters`() {
        myFixture.configureByText(
            "test_file.py", "def test_something(self):\n" +
                    "    <weak_warning descr=\"${TestSmellBundle.message("inspections.magic.number.description")}\">assert \"G\" != 2</weak_warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test magic number wrong class name`() {
        myFixture.configureByText(
            "test_file.py", "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        assert 1 == 1"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WEAK_WARNING })
    }

    @Test
    fun `test magic number multiple`() {
        myFixture.configureByFile("test_magic_number_multiple.py")
        myFixture.checkHighlighting()
    }
}