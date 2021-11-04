package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Ignore
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class SleepyTestTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(SleepyTestTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/sleepy"
    }

    @Ignore
    @Test
    fun `test highlighted sleepy`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "import time\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <warning descr=\"${TestSmellBundle.message("inspections.sleepy.description")}\">time.sleep(5)</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test sleepy without unittest dependency`() {
        myFixture.configureByText(
            "file.py", "import unittest\n" +
                    "import time\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        time.sleep(5)"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    // todo: should pass
    @Ignore
    @Test
    fun `test sleepy multiple`() {
        myFixture.configureByFile("test_sleepy_multiple.py")
        myFixture.checkHighlighting()
    }

}