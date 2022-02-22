package org.jetbrains.research.pynose.plugin.inspections.pytest

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

class DuplicateAssertionTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns TestRunner.PYTEST
        myFixture.enableInspections(DuplicateAssertionTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/duplicate_assertion/pytest"
    }

    @Test
    fun `test duplicate assertion with wrong class name`() {
        myFixture.configureByText(
                "test_file.py", "class SomeClass():\n" +
                "    def test_something(self):\n" +
                "        assert True\n" +
                "        assert True"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }


    @Test
    fun `test duplicate assertion wrong file name`() {
        myFixture.configureByText(
                "just_file.py", "def test_something(self):\n" +
                "    assert True\n" +
                "    assert True"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted duplicate assertion`() {
        myFixture.configureByText(
                "test_file.py", "def test_something(self):\n" +
                "    assert True\n" +
                "    <warning descr=\"${TestSmellBundle.message("inspections.duplicate.description")}\">assert True</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted duplicate assertion with parentheses`() {
        myFixture.configureByText(
            "test_file.py", "def test_something(self):\n" +
                    "    assert (True)\n" +
                    "    <warning descr=\"${TestSmellBundle.message("inspections.duplicate.description")}\">assert (True)</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test duplicates multiple`() {
        myFixture.configureByFile("test_duplicate_multiple.py")
        myFixture.checkHighlighting()
    }
}