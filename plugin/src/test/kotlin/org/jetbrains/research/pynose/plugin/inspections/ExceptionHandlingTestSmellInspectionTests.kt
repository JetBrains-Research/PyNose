package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class ExceptionHandlingTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(ExceptionHandlingTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/exception_handling"
    }

    @Test
    fun `test highlighted try except statement`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.exception.description")}\">try</warning>:\n" +
                    "            x = 3\n" +
                    "        except ValueError:\n" +
                    "            print(\"Try again...\")"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test try statement without unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        try:\n" +
                    "            x = 3\n" +
                    "        except ValueError:\n" +
                    "            print(\"Try again...\")"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted raise statement`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.exception.description")}\">raise</warning> NameError('HiThere')"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test raise statement with non-unittest method name`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        raise NameError('HiThere')"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test exception multiple`() {
        myFixture.configureByFile("test_exception_multiple.py")
        myFixture.checkHighlighting()
    }

}