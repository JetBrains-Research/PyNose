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

class IgnoredTestTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().configureTestRunner(any()) } returns "Unittests"
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner() } returns "Unittests"
        myFixture.enableInspections(IgnoredTestTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/ignored"
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
    fun `test highlighted skip class`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "@unittest.skip(\"reason\")\n" +
                    "class <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "SomeClass</warning>(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted basic skip`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skip(\"reason\")\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "test_something</warning>(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted skip if`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    @unittest.skipIf(mylib.__version__ < (1, 3), \"reason\")\n" +
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
                    "    @unittest.skipUnless(mylib.__version__ < (1, 3), \"reason\")\n" +
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