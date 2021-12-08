package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class SleepyTestTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns "pytest"
        myFixture.enableInspections(SleepyTestTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/sleepy/pytest"
    }

    @Test
    fun `test highlighted sleepy`() {
        myFixture.configureByText(
            "test_file.py", "import time\n" +
                    "def test_something(self):\n" +
                    "    <warning descr=\"${TestSmellBundle.message("inspections.sleepy.description")}\">time.sleep(5)</warning>"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test sleepy wrong class name`() {
        myFixture.configureByText(
            "test_file.py", "import time\n" +
                    "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        time.sleep(5)"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test sleepy multiple`() {
        myFixture.configureByFile("test_sleepy_multiple.py")
        myFixture.checkHighlighting()
    }

}