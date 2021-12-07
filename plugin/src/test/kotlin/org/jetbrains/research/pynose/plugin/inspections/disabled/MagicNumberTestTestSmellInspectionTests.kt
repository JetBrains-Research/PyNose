package org.jetbrains.research.pynose.plugin.inspections.disabled

import com.intellij.lang.annotation.HighlightSeverity
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerGetter
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class MagicNumberTestTestSmellInspectionTests: AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(TestRunnerGetter)
        every { TestRunnerGetter.getConfiguredTestRunner() } returns "Unittests"
        every { TestRunnerGetter.getConfiguredTestRunner() } returns "Unittests"
        myFixture.enableInspections(MagicNumberTestTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/magic_number"
    }

    @Test
    fun `test highlighted magic number`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.magic.number.description")}\">assert 1 == 1</warning>\n" +
                    "        assert \"H\" != \"F\"\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.magic.number.description")}\">self.assertTrue(2 == 1 + 2)</warning>\n"
        )
        
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted combination of numbers and letters`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.magic.number.description")}\">self.assertEqual(\"G\", 2)</warning>"
        )
        
        myFixture.checkHighlighting()
    }

    @Test
    fun `test magic number without unittest dependency`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        assert 1 == 1"
        )
        
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test magic number multiple`() {
        myFixture.configureByFile("test_magic_number_multiple.py")
        
        myFixture.checkHighlighting()
    }
}