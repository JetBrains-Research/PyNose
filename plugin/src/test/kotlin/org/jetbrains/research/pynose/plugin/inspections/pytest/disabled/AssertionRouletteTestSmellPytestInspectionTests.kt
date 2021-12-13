package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunner
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class AssertionRouletteTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any()) } returns TestRunner.PYTEST
        myFixture.enableInspections(AssertionRouletteTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/assertion_roulette/pytest"
    }

    @Test
    fun `test highlighted basic assertion roulette`() {
        myFixture.configureByText(
            "test_file.py",
            "def <weak_warning descr=\"${TestSmellBundle.message("inspections.roulette.description")}\">test_something</weak_warning>(self):\n" +
                    "    assert 1 == 1\n" +
                    "    assert 2 == 2\n" +
                    "    assert 2 == 2"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted basic assertion roulette in class`() {
        myFixture.configureByText(
            "test_file.py", "class TestClass:\n" +
                    "    def <weak_warning descr=\"${TestSmellBundle.message("inspections.roulette.description")}\">test_something</weak_warning>(self):\n" +
                    "        assert 1 == 1\n" +
                    "        assert 2 == 2\n" +
                    "        assert 2 == 2"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test assertions with comments`() {
        myFixture.configureByText(
            "test_file.py", "class TestClass:\n" +
                    "    def test_something(self):\n" +
                    "        assert 1 == 1, \"What's the point?\"\n" +
                    "        assert not 1 == 2\n" +
                    "        assert False, \"Oh no! This assertion failed!\""
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test basic assertion roulette with wrong class name`() {
        myFixture.configureByText(
            "test_file.py", "class SomeClass():\n" +
                    "    def test_something(self):\n" +
                    "        assert 1 == 1\n" +
                    "        assert 2 == 2\n" +
                    "        assert 3 == 3"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WEAK_WARNING })
    }

    @Test
    fun `test assertion roulette multiple`() {
        myFixture.configureByFile("test_roulette_multiple.py")
        myFixture.checkHighlighting()
    }

}