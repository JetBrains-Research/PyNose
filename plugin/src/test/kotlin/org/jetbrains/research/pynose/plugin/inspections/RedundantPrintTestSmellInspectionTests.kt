package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class RedundantPrintTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(RedundantPrintTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/redundant_print"
    }

    @Test
    fun `test highlighted redundant print`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
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
            "file.py", "import unittest\n" +
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