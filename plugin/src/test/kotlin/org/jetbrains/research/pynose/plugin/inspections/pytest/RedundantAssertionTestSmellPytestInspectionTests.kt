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

class RedundantAssertionTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every {
            myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any())
        } returns TestRunner.PYTEST
        myFixture.enableInspections(RedundantAssertionTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/redundant_assertion/pytest"
    }

    @Test
    fun `test highlighted redundant assertion with bool args`() {
        myFixture.configureByText(
            "test_file.py",
            "def test_something(self):\n" + "    <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert True</warning>\n" + "    <warning descr=\"${
                TestSmellBundle.message("inspections.redundant.assertion.description")
            }\">assert False</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted redundant assertion with parenthesis`() {
        myFixture.configureByText(
            "test_file.py",
            "def test_something(self):\n" + "    <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert ((3 == 4))</warning>\n" + "    <warning descr=\"${
                TestSmellBundle.message("inspections.redundant.assertion.description")
            }\">assert (False)</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted redundant assertion with operators`() {
        myFixture.configureByText(
            "test_file.py",
            "def test_something(self):\n" + "    <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert 1 == 1</warning>\n" + "    <warning descr=\"${
                TestSmellBundle.message("inspections.redundant.assertion.description")
            }\">assert 2 <= 2</warning>\n" + "    <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert \"hello\" != \"hello\"</warning>\n" + "    <warning descr=\"${
                TestSmellBundle.message(
                    "inspections.redundant.assertion.description"
                )
            }\">assert 3 is 3</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test non-redundant assertions`() {
        myFixture.configureByText(
            "test_file.py",
            "def test_something(self):\n" + "    <warning descr=\"This statement is unnecessary as it's result will never change\">assert 1 == 2</warning>\n" + "    <warning descr=\"This statement is unnecessary as it's result will never change\">assert 1 <= 2</warning>\n" + "    <warning descr=\"This statement is unnecessary as it's result will never change\">assert True is False</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test redundant assertion wrong class name`() {
        myFixture.configureByText(
            "test_file.py", "class SomeClass():\n" + "    def test_something(self):\n" + "        assert 1 == 1"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test redundant assertion wrong method name`() {
        myFixture.configureByText(
            "test_file.py", "def do_something(self):\n" + "    assert 1 == 1"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test redundant assertion multiple`() {
        myFixture.configureByFile("test_redundant_assertion_multiple.py")
        myFixture.checkHighlighting()
    }
}