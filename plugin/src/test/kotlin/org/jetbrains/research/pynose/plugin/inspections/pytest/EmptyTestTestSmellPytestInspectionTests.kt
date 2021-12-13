package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.inspections.universal.EmptyTestTestSmellInspection
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class EmptyTestTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns "pytest"
        myFixture.enableInspections(EmptyTestTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/empty_test/pytest"
    }

    @Test
    fun `test highlighted empty tests`() {
        myFixture.configureByText(
                "test_file.py",
                "def <warning descr=\"${TestSmellBundle.message("inspections.empty.description")}\">test_something</warning>(self):\n" +
                        "    pass\n" +
                        "def <warning descr=\"${TestSmellBundle.message("inspections.empty.description")}\">test_something_else</warning>(self):\n" +
                        "    pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test empty wrong filename`() {
        myFixture.configureByText(
                "file.py", "import unittest\n" +
                "class SomeClass():\n" +
                "    def test_something(self):\n" +
                "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test empty multiple`() {
        myFixture.configureByFile("test_empty_multiple.py")
        myFixture.checkHighlighting()
    }

}