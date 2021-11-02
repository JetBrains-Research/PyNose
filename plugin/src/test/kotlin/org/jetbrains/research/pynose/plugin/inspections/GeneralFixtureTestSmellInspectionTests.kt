package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class GeneralFixtureTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
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
    fun `test general fixture without unittest dependency`() {
        myFixture.configureByFile("test_general_fixture_no_dependency.py")
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test general fixture normal`() {
        myFixture.configureByFile("test_general_fixture_normal.py")
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }
}