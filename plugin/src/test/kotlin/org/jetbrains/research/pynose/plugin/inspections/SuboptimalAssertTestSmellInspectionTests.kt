package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class SuboptimalAssertTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(PyNoseMode)
        every { PyNoseMode.getPyNoseUnittestMode() } returns true
        every { PyNoseMode.getPyNosePytestMode() } returns false
        myFixture.enableInspections(SuboptimalAssertTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/suboptimal_assert"
    }

    @Test
    fun `test highlighted suboptimal equality`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        X = 5\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertEqual(X, False)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertEqual(X, True)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertNotEqual(X, False)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertNotEqual(X, True)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertEqual(X, None)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertNotEqual(X, None)</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test correct suboptimal equality replacements`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        X = 5\n" +
                    "        assertFalse(X)\n" +
                    "        assertTrue(X)"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted suboptimal is`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        X = 5\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertIs(X, True)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertIsNot(X, False)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertIsNot(X, None)</warning>"
        )
        myFixture.checkHighlighting()
    }


    @Test
    fun `test correct suboptimal is replacements`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        X = 5\n" +
                    "        assertIsNone(X)\n" +
                    "        assertIsNotNone(X)"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted suboptimal comparison`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        X = 5\n" +
                    "        Y = 6\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertTrue(X != Y)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertFalse(X == Y)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertTrue(X < Y)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertFalse(X >= Y)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertTrue(X not in Y)</warning>\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">assertFalse(X in Y)</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test correct suboptimal comparison replacements`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        X = 5\n" +
                    "        Y = 6\n" +
                    "        assertEqual(X, Y)\n" +
                    "        assertNotEqual(X, Y)\n" +
                    "        assertIs(X, Y)\n" +
                    "        assertIsNot(X, Y)\n" +
                    "        assertLessEqual(X, Y)\n" +
                    "        assertGreater(X, Y)\n" +
                    "        assertIn(X, Y)\n" +
                    "        assertNotIn(X, Y)"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test suboptimal assertion without unittest dependency`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        X = 5\n" +
                    "        Y = 6\n" +
                    "        assertTrue(X is Y)\n" +
                    "        assertTrue(X is not Y)\n" +
                    "        assertEqual(X, None)\n" +
                    "        assertNotEqual(X, None)\n" +
                    "        assertTrue(X <= Y)\n" +
                    "        assertFalse(X > Y)"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test suboptimal assertion with non-unittest function name`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    def do_something(self):\n" +
                    "        X = 5\n" +
                    "        Y = 6\n" +
                    "        assertTrue(X is Y)\n" +
                    "        assertTrue(X is not Y)\n" +
                    "        assertEqual(X, None)\n" +
                    "        assertNotEqual(X, None)\n" +
                    "        assertTrue(X <= Y)\n" +
                    "        assertFalse(X > Y)"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test suboptimal multiple`() {
        myFixture.configureByFile("test_suboptimal_multiple.py")
        myFixture.checkHighlighting()
    }

}