package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.unittest.AssertionRouletteTestSmellUnittestInspection
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class AssertionRouletteTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(PyNoseMode)
        every { PyNoseMode.getPyNoseUnittestMode() } returns true
        every { PyNoseMode.getPyNosePytestMode() } returns false
        myFixture.enableInspections(AssertionRouletteTestSmellUnittestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/assertion_roulette/unittest"
    }

    @Test
    fun `test highlighted basic assertion roulette`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.roulette.description")}\">test_something</warning>(self):\n" +
                    "        assert 1 == 1\n" +
                    "        assert 2 == 2\n" +
                    "        assert 2 == 2"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted different assertion types roulette`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.roulette.description")}\">test_something</warning>(self):\n" +
                    "        assert 1 == 1\n" +
                    "        self.assertFalse(1 == 2)\n"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test assertions with comments`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        assert 1 == 1, \"What's the point?\"\n" +
                    "        self.assertFalse(1 == 2)\n" +
                    "        assert False, \"Oh no! This assertion failed!\""
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted unittest assertions roulette`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.roulette.description")}\">test_something</warning>(self):\n" +
                    "        self.assertTrue(1 == 1)\n" +
                    "        self.assertFalse(1 == 2)\n" +
                    "        self.assertEquals(1 + 2, 3)"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test basic assertion roulette without unittest dependency`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        assert 1 == 1\n" +
                    "        assert 2 == 2\n" +
                    "        assert 3 == 3"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test assertion roulette multiple`() {
        myFixture.configureByFile("test_roulette_multiple.py")
        myFixture.checkHighlighting()
    }

}