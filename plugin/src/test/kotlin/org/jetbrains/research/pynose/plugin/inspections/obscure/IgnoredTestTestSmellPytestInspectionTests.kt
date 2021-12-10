package org.jetbrains.research.pynose.plugin.inspections.obscure

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import io.mockk.every
import io.mockk.mockkObject
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspectionTestWithSdk
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.junit.Test
import org.junit.jupiter.api.BeforeAll

class IgnoredTestTestSmellPytestInspectionTests : AbstractTestSmellInspectionTestWithSdk() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
        mockkObject(myFixture.project.service<TestRunnerServiceFacade>())
        every {
            myFixture.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(any())
        } returns "pytest"
        myFixture.enableInspections(IgnoredTestTestSmellPytestInspection())
    }

    override fun getTestDataPath(): String {
        return "src/test/resources/org/jetbrains/research/pynose/plugin/inspections/data/ignored/pytest"
    }

    @Test
    fun `test skipped wrong class name`() {
        myFixture.configureByText(
            "test_file.py", "import pytest\n" +
                    "class TestClass:\n" +
                    "    @pytest.mark.skip(reason=\"reason\")\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test not skipped`() {
        myFixture.configureByText(
            "test_file.py", "class SomeTestCase:\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test skip class with comment`() {
        myFixture.configureByText(
            "test_file.py", "import pytest\n" +
                    "@pytest.mark.skip(reason=\"reason\")\n" +
                    "class TestClass(unittest.TestCase):\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted skipped class`() {
        myFixture.configureByText(
            "test_file.py", "import pytest\n" +
                    "@pytest.mark.skip()\n" +
                    "class <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "TestClass</warning>():\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test basic skip with comment`() {
        myFixture.configureByText(
            "test_file.py", "import pytest\n" +
                    "class TestClass:\n" +
                    "    @pytest.mark.skip(reason=\"reason\")\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted basic skip`() {
        myFixture.configureByText(
            "test_file.py", "import pytest\n" +
                    "class TestClass:\n" +
                    "    @pytest.mark.skip()\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "test_something</warning>(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test skip if with comment`() {
        myFixture.configureByText(
            "test_file.py", "import pytest\n" +
                    "class TestClass:\n" +
                    "    @pytest.mark.skipIf(mylib.__version__ < (1, 3), reason=\"reason\")\n" +
                    "    def test_something(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted skip if`() {
        myFixture.configureByText(
            "test_file.py", "import pytest\n" +
                    "class TestClass:\n" +
                    "    @pytest.mark.skipIf(mylib.__version__ < (1, 3))\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "test_something</warning>(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test highlighted xfail`() {
        myFixture.configureByText(
            "test_file.py", "import pytest\n" +
                    "class TestClass:\n" +
                    "    @pytest.mark.xfail(mylib.__version__ < (1, 3))\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "test_something</warning>(self):\n" +
                    "        pass"
        )
        myFixture.checkHighlighting()
    }

    @Test
    fun `test xfail with comment`() {
        myFixture.configureByText(
            "test_file.py", "import pytest\n" +
                    "class TestClass:\n" +
                    "    @pytest.mark.xfail(mylib.__version__ < (1, 3), reason=\"reason\")\n" +
                    "    def <warning descr=\"${TestSmellBundle.message("inspections.ignored.description")}\">" +
                    "test_something</warning>(self):\n" +
                    "        pass"
        )
        val highlightInfos = myFixture.doHighlighting()
        assertTrue(!highlightInfos.any { it.severity == HighlightSeverity.WARNING })
    }

    @Test
    fun `test ignored tests multiple`() {
        myFixture.configureByFile("test_ignored_multiple.py")
        myFixture.checkHighlighting()
    }

}