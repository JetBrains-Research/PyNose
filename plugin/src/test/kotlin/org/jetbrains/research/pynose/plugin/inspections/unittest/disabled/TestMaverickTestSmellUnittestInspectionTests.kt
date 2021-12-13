package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class TestMaverickTestSmellUnittestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns "Unittests"
        myFixture.enableInspections(TestMaverickTestSmellUnittestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/maverick/unittest"
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
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WEAK_WARNING })
    }

    @Test
    fun `test maverick normal`() {
        myFixture.configureByFile("test_maverick_normal.py")
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WEAK_WARNING })
    }
}