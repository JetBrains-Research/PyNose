package org.jetbrains.research.pynose.plugin.inspections.disabled

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class ConditionalTestLogicTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/conditional_logic"
    }

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns "Unittests"
        myFixture.enableInspections(ConditionalTestLogicTestSmellInspection())
    }

    @Test
    fun `test highlighted if statement`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.conditional.description")}\">" +
                    "if</warning> (2 > 1):" +
                    "            pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test if statement without unittest dependency`() {
        myFixture.configureByText(
            "test_file.py", "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        if (2 > 1):" +
                    "            pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted for statement`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.conditional.description")}\">" +
                    "for</warning> _ in range(10):" +
                    "            pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test for statement with non-unittest name`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def do_something(self):\n" +
                    "        for _ in range(10):" +
                    "            pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted while statement`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        x = 0\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.conditional.description")}\">" +
                    "while</warning> x < 10:" +
                    "            x += 1"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted list comprehension`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        S = <warning descr=\"${TestSmellBundle.message("inspections.conditional.description")}\">" +
                    "[x**2 for x in range(10)]</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted set comprehension`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        x = [10, '30', 30, 10, '56']\n" +
                    "        ux = <warning descr=\"${TestSmellBundle.message("inspections.conditional.description")}\">" +
                    "{int(xx) for xx in x}</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted dict comprehension`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        S = <warning descr=\"${TestSmellBundle.message("inspections.conditional.description")}\">" +
                    "{num: num**2 for num in range(1, 11)}</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted generator`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        S = list(<warning descr=\"${TestSmellBundle.message("inspections.conditional.description")}\">" +
                    "2 * n for n in range(50)</warning>)"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test conditional multiple`() {
        myFixture.configureByFile("test_conditional_multiple.py")
        myFixture.checkHighlighting()
    }
}