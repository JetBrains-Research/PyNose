package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunner
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class SuboptimalAssertTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns TestRunner.UNITTESTS
        myFixture.enableInspections(SuboptimalAssertTestSmellUnittestInspection())
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
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertEqual(X, False)</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertEqual((X), (True))</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertNotEqual(X, False)</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertNotEqual(True, X)</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertEqual((X), None)</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertNotEqual(X, (None))</warning>"
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
                "        self.assertFalse(X)\n" +
                "        self.assertTrue(X)"
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
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertIs(X, True)</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertIsNot(X, False)</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertIsNot(X, None)</warning>"
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
                "        self.assertIsNone(X)\n" +
                "        self.assertIsNotNone(X)"
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
                "        Z = {4, 6}\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertTrue((X != Y))</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertFalse((X) == (Y))</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertTrue((X) < Y)</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertFalse(X >= (Y))</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertTrue(X not in Z)</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.suboptimal.description")}\">self.assertFalse(X in Z)</warning>"
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
                "        Z = {5, 7}\n" +
                "        self.assertEqual(X, Y)\n" +
                "        self.assertNotEqual(X, Y)\n" +
                "        self.assertIs(X, Y)\n" +
                "        self.assertIsNot(X, Y)\n" +
                "        self.assertLessEqual(X, Y)\n" +
                "        self.assertGreater(X, Y)\n" +
                "        self.assertIn(X, Z)\n" +
                "        self.assertNotIn(Y, Z)"
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
                "        self.assertTrue(X is Y)\n" +
                "        self.assertTrue(X is not Y)\n" +
                "        self.assertEqual(X, None)\n" +
                "        self.assertNotEqual(X, None)\n" +
                "        self.assertTrue(X <= Y)\n" +
                "        self.assertFalse(X > Y)"
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
                "        self.assertTrue(X is Y)\n" +
                "        self.assertTrue(X is not Y)\n" +
                "        self.assertEqual(X, None)\n" +
                "        self.assertNotEqual(X, None)\n" +
                "        self.assertTrue(X <= Y)\n" +
                "        self.assertFalse(X > Y)"
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