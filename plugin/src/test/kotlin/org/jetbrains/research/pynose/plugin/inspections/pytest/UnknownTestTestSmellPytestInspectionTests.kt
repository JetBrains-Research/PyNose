package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class UnknownTestTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every { myFixture.project.service<TestRunnerServiceFacade>().configureTestRunner(any()) } returns "pytest"
        every { myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner() } returns "pytest"
        myFixture.enableInspections(UnknownTestTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/unknown_test/pytest"
    }

    @Test
    fun `test highlighted unknown test`() {
        myFixture.configureByText(
                "test_file.py",
                "def <warning descr=\"${TestSmellBundle.message("inspections.unknown.description")}\">test_something</warning>(self):\n" +
                        "    pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted several unknown tests`() {
        myFixture.configureByText(
                "test_file.py", "def <warning descr=\"${TestSmellBundle.message("inspections.unknown.description")}\">test_something</warning>(self):\n" +
                "    pass\n\n" +
                "def <warning descr=\"${TestSmellBundle.message("inspections.unknown.description")}\">test_something_else</warning>(self):\n" +
                "    x = 1"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test case with assertion in it`() {
        myFixture.configureByText(
                "test_file.py", "def test_something(self):\n" +
                "    assert 1 == 2\n\n" +
                "def test_something_else(self):\n" +
                "    assert 1 == 1"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test unknown with wrong class name`() {
        myFixture.configureByText(
                "test_file.py", "class SomeClass():\n" +
                "    def test_something(self):\n" +
                "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test unknown multiple`() {
        myFixture.configureByFile("test_unknown_multiple.py")
        myFixture.checkHighlighting()
    }
}