package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.lang.annotation.HighlightSeverity
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class IgnoredTestTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(PyNoseMode)
        every { PyNoseMode.getPyNoseUnittestMode() } returns true
        every { PyNoseMode.getPyNosePytestMode() } returns false
        myFixture.enableInspections(IgnoredTestTestSmellUnittestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/ignored/unittest"
    }

    @Test
    fun `test skipped without unittest dependency`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
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
            "test_file.py", "import unittest\n" +
                    "class SomeTestCase(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test skip class with comment`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "@unittest.skip(\"reason\")\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted skip class`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "@unittest.skip()\n" +
                    "class <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "SomeClass</warning>(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test basic skip with comment`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skip(\"reason\")\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted basic skip`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skip()\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "test_something</warning>(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test skip if with comment`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skipIf(mylib.__version__ < (1, 3), \"reason\")\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted skip if`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skipIf(mylib.__version__ < (1, 3))\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "test_something</warning>(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted skip unless`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skipUnless(mylib.__version__ < (1, 3))\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "test_something</warning>(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test ignored tests multiple`() {
        myFixture.configureByFile("test_ignored_multiple.py")
        myFixture.checkHighlighting()
    }

}