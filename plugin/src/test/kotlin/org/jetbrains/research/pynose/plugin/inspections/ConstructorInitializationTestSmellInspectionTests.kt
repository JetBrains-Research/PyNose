package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class ConstructorInitializationTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/constructor"
    }

    @BeforeAll
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(ConstructorInitializationTestSmellInspection())
    }

    @Test
    fun `test constructor highlighting`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def <warning descr=\"Test smell: Constructor Initialization Test in class `SomeClass`\">" +
                    "__init__</warning>(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test constructor without unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
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
            "file.py", "import unittest\n" +
                    "class SomeTestCase(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test no highlighting transitive`() {
        myFixture.configureByFile("test_constructor_transitive.py")
        myFixture.checkHighlighting()
    }

}