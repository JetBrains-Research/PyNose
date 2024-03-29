package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunner
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.inspections.universal.SleepyTestTestSmellInspection
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class SleepyTestTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every {
            myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any())
        } returns TestRunner.UNITTESTS
        myFixture.enableInspections(SleepyTestTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/sleepy/unittest"
    }

    @Test
    fun `test highlighted sleepy`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "import time\n" +
                    "class SomeClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        <weak_warning descr=\"${TestSmellBundle.message("inspections.sleepy.description")}\">time.sleep(5)</weak_warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test sleepy without unittest dependency`() {
        myFixture.configureByText(
            "test_file.py", "import unittest\n" +
                    "import time\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        time.sleep(5)"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WEAK_WARNING })
    }

    @Test
    fun `test sleepy multiple`() {
        myFixture.configureByFile("test_sleepy_multiple.py")
        myFixture.checkHighlighting()
    }

}