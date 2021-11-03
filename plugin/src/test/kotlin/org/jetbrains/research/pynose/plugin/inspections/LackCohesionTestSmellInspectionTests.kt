package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class LackCohesionTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LackCohesionTestSmellInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/lack_cohesion"
    }

    @Test
    fun `test highlighted lack of cohesion`() {
        myFixture.configureByFile("test_lack_cohesion.py")
        myFixture.checkHighlighting()
    }

    @Test
    fun `test lack of cohesion without unittest dependency`() {
        myFixture.configureByFile("test_lack_cohesion_no_dependency.py")
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test normal cohesion`() {
        myFixture.configureByFile("test_normal_cohesion.py")
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }
}