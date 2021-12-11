package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class RedundantPrintTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns "Unittests"
        myFixture.enableInspections(RedundantPrintTestSmellUnittestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/redundant_print/unittest"
    }

    @Test
    fun `test highlighted redundant print`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.print.description")}\">print(\"hello!\")</warning>\n" +
                    "        assert 1 == 1\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.print.description")}\">print(\"hello again!\")</warning>"
        )
        
        myFixture.checkHighlighting()
    }

    @Test
    fun `test redundant print without unittest dependency`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass():\n" +
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