package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class RedundantAssertionTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(RedundantAssertionTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/redundant_assertion"
    }

    @Test
    fun `test highlighted redundant assertion with bool args`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert True</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert False</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted combination of numbers and letters`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"This statement is unnecessary as it's result will never change\">self.assertTrue(True)</warning>\n" +
                    "        self.assertEqual(\"G\", 2)\n" +
                    "        self.assertTrue(1 == 1)"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted redundant assertion with operators`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert 1 == 1</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert 2 <= 2</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert \"hello\" != \"hello\"</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.redundant.assertion.description")}\">assert 3 is 3</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test non-redundant assertions with`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        assert 1 == 2\n" +
                    "        assert 1 <= 2\n" +
                    "        assert True is False"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test redundant assertion without unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        assert 1 == 1"
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