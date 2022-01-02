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
            Pair(ConditionalTestLogicTestSmellInspection(), "Conditional logic"),
            Pair(EmptyTestTestSmellInspection(), "Empty test"),
            Pair(ExceptionHandlingTestSmellInspection(), "Exception handling"),
            Pair(MagicNumberTestTestSmellPytestInspection(), "Magic number"),
            Pair(RedundantAssertionTestSmellPytestInspection(), "Redundant assertion"),
            Pair(RedundantPrintTestSmellInspection(), "Redundant print"),
            Pair(SleepyTestTestSmellInspection(), "Sleepy test")
        )
    }

    fun getPytestInspectionsFileLevel(): List<PyInspection> {
        return listOf<PyInspection>(
            AssertionRouletteTestSmellPytestInspection(),
            DuplicateAssertionTestSmellPytestInspection(),
            LackCohesionTestSmellPytestInspection(),
            ObscureInLineSetupTestSmellPytestInspection(),
            TestMaverickTestSmellPytestInspection()
        )
    }

    fun getUnittestInspections(): List<PyInspection> {
        return listOf<PyInspection>(
            AssertionRouletteTestSmellUnittestInspection(),
            ConditionalTestLogicTestSmellInspection(),
            ConstructorInitializationTestSmellUnittestInspection(),
            DefaultTestTestSmellUnittestInspection(),
            DuplicateAssertionTestSmellUnittestInspection(),
            EmptyTestTestSmellInspection(),
            ExceptionHandlingTestSmellInspection(),
            LackCohesionTestSmellUnittestInspection(),
            ObscureInLineSetupTestSmellUnittestInspection(),
            MagicNumberTestTestSmellUnittestInspection(),
            RedundantAssertionTestSmellUnittestInspection(),
            RedundantPrintTestSmellInspection(),
            SleepyTestTestSmellInspection(),
            SuboptimalAssertTestSmellUnittestInspection(),
            TestMaverickTestSmellUnittestInspection()
        )
    }
}