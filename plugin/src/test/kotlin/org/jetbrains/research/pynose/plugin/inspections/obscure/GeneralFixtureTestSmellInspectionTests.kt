package org.jetbrains.research.pynose.plugin.inspections.obscure

import com.intellij.lang.annotation.HighlightSeverity
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerGetter
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class GeneralFixtureTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(TestRunnerGetter)
        every { TestRunnerGetter.getConfiguredTestRunner() } returns "Unittests"
        every { TestRunnerGetter.getConfiguredTestRunner() } returns "Unittests"
        myFixture.enableInspections(GeneralFixtureTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/general_fixture"
    }

    @Test
    fun `test highlighted general fixture basic`() {
        myFixture.configureByFile("test_general_fixture_basic.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted general fixture more complicated`() {
        myFixture.configureByFile("test_general_fixture_complicated.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted general fixture multiple`() {
        myFixture.configureByFile("test_general_fixture_multiple.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test general fixture without unittest dependency`() {
        myFixture.configureByFile("test_general_fixture_no_dependency.py")
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test general fixture normal`() {
        println("here")
        myFixture.configureByFile("test_general_fixture_normal.py")
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }
}