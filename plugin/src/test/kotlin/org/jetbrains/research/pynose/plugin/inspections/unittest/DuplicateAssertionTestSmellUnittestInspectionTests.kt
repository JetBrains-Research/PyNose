package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class DuplicateAssertionTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns "Unittests"
        myFixture.enableInspections(DuplicateAssertionTestSmellUnittestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/duplicate_assertion/unittest"
    }

    @Test
    fun `test duplicate assertion without unittest dependency`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class SomeClass():\n" +
                "    def test_something(self):\n" +
                "        assert True\n" +
                "        assert True"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test highlighted duplicate assertion`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class SomeClass(unittest.TestCase):\n" +
                "    def test_something(self):\n" +
                "        assert True\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.duplicate.description")}\">assert True</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test duplicate assertion in different functions`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class SomeClass(unittest.TestCase):\n" +
                "    def test_something(self):\n" +
                "        assert True\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.duplicate.description")}\">assert True</warning>\n\n" +
                "    def test_something_else(self):\n" +
                "        assert True\n"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted duplicate unittest assertion`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
                "class SomeClass(unittest.TestCase):\n" +
                "    def test_something(self):\n" +
                "        self.assertTrue(1 == 2)\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.duplicate.description")}\">self.assertTrue(1 == 2)</warning>\n" +
                "        <warning descr=\"${TestSmellBundle.message("inspections.duplicate.description")}\">self.assertTrue(1 == 2)</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test duplicates multiple`() {
        myFixture.configureByFile("test_duplicate_multiple.py")
        myFixture.checkHighlighting()
    }
}