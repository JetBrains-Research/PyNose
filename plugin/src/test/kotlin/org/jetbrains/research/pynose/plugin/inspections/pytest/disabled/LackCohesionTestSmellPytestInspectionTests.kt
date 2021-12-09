package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class LackCohesionTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every {
            myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any())
        } returns "pytest"
        myFixture.enableInspections(LackCohesionTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/lack_cohesion/pytest"
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

    @Test
    fun `test lack cohesion multiple`() {
        myFixture.configureByFile("test_lack_cohesion_multiple.py")
        myFixture.checkHighlighting()
    }

    // todo does not pass yet, even with the annotation
    /*
    @Disabled("Fix todo first")
    @Test
    fun `test lack cohesion in a file`() {
        myFixture.configureByFile("test_lack_cohesion_file.py")
        myFixture.checkHighlighting()
    }
    */
}