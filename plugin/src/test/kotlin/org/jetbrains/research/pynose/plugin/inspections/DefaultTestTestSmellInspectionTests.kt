package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class DefaultTestTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/default"
    }

    @BeforeAll
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(DefaultTestTestSmellInspection())
    }

    @Test
    fun `test default highlighting transitive`() {
        myFixture.configureByFile("test_default_transitive.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test default name without unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class MyTestCase():\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test not default name with unittest dependency`() {
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
    fun `test default highlighting`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class <warning descr=\"Consider changing the name of your test suite to a non-default one " +
                    "to better reflect its content\">MyTestCase" +
                    "</warning>(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

}