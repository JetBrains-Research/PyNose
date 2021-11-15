package org.jetbrains.research.pynose.plugin.inspections

import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class UnknownTestTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(PyNoseMode)
        every { PyNoseMode.getPyNoseUnittestMode() } returns true
        every { PyNoseMode.getPyNosePytestMode() } returns false
        myFixture.enableInspections(UnknownTestTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/unknown_test"
    }

    @Test
    fun `test highlighted unknown test`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.unknown.description")}\">test_something</warning>(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted several unknown tests`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.unknown.description")}\">test_something</warning>(self):\n" +
                    "        pass\n\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.unknown.description")}\">test_something_else</warning>(self):\n" +
                    "        x = 1"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test case with assertion in it`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        assert 1 == 2\n\n" +
                    "    def test_something(self):\n" +
                    "        self.assertEqual(1, 1)"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test unknown without unittest dependency`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test unknown multiple`() {
        myFixture.configureByFile("test_unknown_multiple.py")
        myFixture.checkHighlighting()
    }
}