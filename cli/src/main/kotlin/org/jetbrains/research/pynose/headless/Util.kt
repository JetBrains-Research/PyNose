package org.jetbrains.research.pynose.headless

import com.jetbrains.python.inspections.PyInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.DuplicateAssertionTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.MagicNumberTestTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.RedundantAssertionTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.disabled.AssertionRouletteTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.disabled.LackCohesionTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.disabled.ObscureInLineSetupTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.disabled.TestMaverickTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.unittest.*
import org.jetbrains.research.pynose.plugin.inspections.unittest.disabled.AssertionRouletteTestSmellUnittestInspection
import org.jetbrains.research.pynose.plugin.inspections.unittest.disabled.LackCohesionTestSmellUnittestInspection
import org.jetbrains.research.pynose.plugin.inspections.unittest.disabled.ObscureInLineSetupTestSmellUnittestInspection
import org.jetbrains.research.pynose.plugin.inspections.unittest.disabled.TestMaverickTestSmellUnittestInspection
import org.jetbrains.research.pynose.plugin.inspections.universal.*

object Util {
    fun getPytestInspectionsFunctionLevel(): Map<PyInspection, String> {
        return mapOf(
            Pair(ConditionalTestLogicTestSmellInspection(), "Conditional test logic"),
            Pair(EmptyTestTestSmellInspection(), "Empty test"),
            Pair(ExceptionHandlingTestSmellInspection(), "Exception handling"),
            Pair(MagicNumberTestTestSmellPytestInspection(), "Magic number"),
            Pair(RedundantAssertionTestSmellPytestInspection(), "Redundant assertion")
        )
    }

    fun getPytestInspectionsFileLaunchLevel(): Map<PyInspection, String> {
        return mapOf(
            Pair(AssertionRouletteTestSmellPytestInspection(), "Assertion roulette"),
            Pair(DuplicateAssertionTestSmellPytestInspection(), "Duplicate assertion"),
            Pair(ObscureInLineSetupTestSmellPytestInspection(), "Obscure in-line setup")
        )
    }

    fun getPytestInspectionsFileResultLevel(): Map<PyInspection, String> {
        return mapOf(
            Pair(LackCohesionTestSmellPytestInspection(), "Lack cohesion"),
            Pair(TestMaverickTestSmellPytestInspection(), "Test maverick")
        )
    }

    fun getUnittestInspections(): Map<PyInspection, String> {
        return mapOf(
            Pair(AssertionRouletteTestSmellUnittestInspection(), "Assertion roulette"),
            Pair(ConditionalTestLogicTestSmellInspection(), "Conditional test logic"),
            Pair(ConstructorInitializationTestSmellUnittestInspection(), "Constructor initialization"),
            Pair(DefaultTestTestSmellUnittestInspection(), "Default test"),
            Pair(DuplicateAssertionTestSmellUnittestInspection(), "Duplicate assertion"),
            Pair(EmptyTestTestSmellInspection(), "Empty test"),
            Pair(ExceptionHandlingTestSmellInspection(), "Exception handling"),
            Pair(LackCohesionTestSmellUnittestInspection(), "Lack cohesion"),
            Pair(ObscureInLineSetupTestSmellUnittestInspection(), "Obscure in-line setup"),
            Pair(MagicNumberTestTestSmellUnittestInspection(), "Magic number"),
            Pair(RedundantAssertionTestSmellUnittestInspection(), "Redundant assertion"),
            Pair(SuboptimalAssertTestSmellUnittestInspection(), "Suboptimal assertion"),
            Pair(TestMaverickTestSmellUnittestInspection(), "Test maverick")
        )
    }

    fun getUniversalNonRecursiveInspections(): Map<PyInspection, String> {
        return mapOf(
            Pair(RedundantPrintTestSmellInspection(), "Redundant print"),
            Pair(SleepyTestTestSmellInspection(), "Sleepy test")
        )
    }
}