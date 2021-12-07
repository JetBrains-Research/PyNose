package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class ConstructorInitializationTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/constructor"
    }

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().configureTestRunner(any()) } returns "Unittests"
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner() } returns "Unittests"
        myFixture.enableInspections(ConstructorInitializationTestSmellUnittestInspection())
    }

    @Test
    fun `test constructor highlighting`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class SomeClass(unittest.TestCase):\n" +
                "    def <warning descr=\"${TestSmellBundle.message("inspections.constructor.initialization.description")}\">" +
                "__init__</warning>(self):\n" +
                "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test constructor without unittest dependency`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class SomeClass():\n" +
                "    def __init__(self):\n" +
                "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test not constructor with unittest dependency`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class SomeTestCase(unittest.TestCase):\n" +
                "    def test_something(self):\n" +
                "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test constructor transitive`() {
        myFixture.configureByFile("test_constructor_transitive.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test constructor multiple`() {
        myFixture.configureByFile("test_constructor_multiple.py")
        myFixture.checkHighlighting()
    }

}