package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.lang.annotation.HighlightSeverity
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class DefaultTestTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/default"
    }

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(PyNoseMode)
        every { PyNoseMode.getPyNoseUnittestMode() } returns true
        every { PyNoseMode.getPyNosePytestMode() } returns false
        myFixture.enableInspections(DefaultTestTestSmellUnittestInspection())
    }

    @Test
    fun `test default highlighting transitive`() {
        myFixture.configureByFile("test_default_transitive.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test default name without unittest dependency`() {
        myFixture.configureByText(
                "test_file.py", "import unittest\n" +
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
                "test_file.py", "import unittest\n" +
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
                "test_file.py", "import unittest\n" +
                "class <warning descr=\"${TestSmellBundle.message("inspections.default.description")}\">MyTestCase" +
                "</warning>(unittest.TestCase):\n" +
                "    def test_something(self):\n" +
                "        pass"
        )
        myFixture.checkHighlighting()
    }

}