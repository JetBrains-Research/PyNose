package org.jetbrains.research.pynose.plugin.inspections.disabled

import com.intellij.lang.annotation.HighlightSeverity
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerGetter
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class TestMaverickTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(TestRunnerGetter)
        every { TestRunnerGetter.getTestRunner() } returns "Unittests"
        every { TestRunnerGetter.getConfiguredTestRunner() } returns "Unittests"
        myFixture.enableInspections(TestMaverickTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/maverick"
    }

    @Test
    fun `test highlighted maverick basic`() {
        myFixture.configureByFile("test_maverick_basic.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted maverick more complicated`() {
        myFixture.configureByFile("test_maverick_complicated.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted maverick multiple`() {
        myFixture.configureByFile("test_maverick_multiple.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test maverick without unittest dependency`() {
        myFixture.configureByFile("test_maverick_no_dependency.py")
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test maverick normal`() {
        myFixture.configureByFile("test_maverick_normal.py")
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }
}