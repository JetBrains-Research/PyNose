package org.jetbrains.research.pynose.plugin.inspections.unittest

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

class DefaultTestTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/default"
    }

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns TestRunner.UNITTESTS
        myFixture.enableInspections(DefaultTestTestSmellUnittestInspection())
    }

    @Test
    fun `test default highlighting transitive`() {
        myFixture.configureByFile("test_default_transitive.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test default name without unittest dependency`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class MyTestCase():\n" +
                "    def test_something(self):\n" +
                "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WEAK_WARNING })
    }

    @Test
    fun `test not default name with unittest dependency`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class SomeTestCase(unittest.TestCase):\n" +
                "    def test_something(self):\n" +
                "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WEAK_WARNING })
    }

    @Test
    fun `test default highlighting`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class <weak_warning descr=\"${TestSmellBundle.message("inspections.default.description")}\">MyTestCase" +
                "</weak_warning>(unittest.TestCase):\n" +
                "    def test_something(self):\n" +
                "        pass"
        )
        myFixture.checkHighlighting()
    }

}