package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class IgnoredTestTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(IgnoredTestTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/ignored"
    }

    @Test
    fun `test skipped without unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    @unittest.skip(\"reason\")\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test not skipped with unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeTestCase(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test skip class with unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "<warning descr=\"Test smell: Ignored Test in class `SomeClass`\">" +
                    "@unittest.skip(\"reason\")\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        pass</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test basic skip with unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "<warning descr=\"Test smell: Ignored Test in class `SomeClass`\">" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skip(\"reason\")\n" +
                    "    def test_something(self):\n" +
                    "        pass</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test skip if with unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "<warning descr=\"Test smell: Ignored Test in class `SomeClass`\">" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skipIf(mylib.__version__ < (1, 3), \"reason\")\n" +
                    "    def test_something(self):\n" +
                    "        pass</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test skip unless with unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "<warning descr=\"Test smell: Ignored Test in class `SomeClass`\">" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skipUnless(mylib.__version__ < (1, 3), \"reason\")\n" +
                    "    def test_something(self):\n" +
                    "        pass</warning>"
        )
        myFixture.checkHighlighting()
    }

}