package org.jetbrains.research.pynose.headless

import com.jetbrains.python.inspections.PyInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.DuplicateAssertionTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.RedundantAssertionTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.disabled.*
import org.jetbrains.research.pynose.plugin.inspections.unittest.*
import org.jetbrains.research.pynose.plugin.inspections.unittest.disabled.*
import org.jetbrains.research.pynose.plugin.inspections.universal.*

object HeadlessInspectionsUtils {
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

    fun getUnittestInspectionsFunctionResultLevel(): Map<PyInspection, String> {
        return mapOf(
            Pair(AssertionRouletteTestSmellUnittestInspection(), "Assertion roulette"),
            Pair(ConditionalTestLogicTestSmellInspection(), "Conditional test logic"),
            Pair(ConstructorInitializationTestSmellUnittestInspection(), "Constructor initialization"),
            Pair(DuplicateAssertionTestSmellUnittestInspection(), "Duplicate assertion"),
            Pair(EmptyTestTestSmellInspection(), "Empty test"),
            Pair(ExceptionHandlingTestSmellInspection(), "Exception handling"),
            Pair(ObscureInLineSetupTestSmellUnittestInspection(), "Obscure in-line setup"),
            Pair(MagicNumberTestTestSmellUnittestInspection(), "Magic number"),
            Pair(RedundantAssertionTestSmellUnittestInspection(), "Redundant assertion"),
            Pair(SuboptimalAssertTestSmellUnittestInspection(), "Suboptimal assertion")
        )
    }

    fun getUnittestInspectionsClassResultLevel(): Map<PyInspection, String> {
        return mapOf(
            Pair(DefaultTestTestSmellUnittestInspection(), "Default test"),
            Pair(LackCohesionTestSmellUnittestInspection(), "Lack cohesion"),
            Pair(TestMaverickTestSmellUnittestInspection(), "Test maverick")
        )
    }

    fun getUniversalInspections(): Map<PyInspection, String> {
        return mapOf(
            Pair(RedundantPrintTestSmellInspection(), "Redundant print"),
            Pair(SleepyTestTestSmellInspection(), "Sleepy test")
        )
    }
}