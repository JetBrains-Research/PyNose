package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.lang.annotation.HighlightSeverity
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class TestMaverickTestSmellInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
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