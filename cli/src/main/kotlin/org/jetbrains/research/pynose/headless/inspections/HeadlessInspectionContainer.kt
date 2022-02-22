package org.jetbrains.research.pynose.headless.inspections

import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.DuplicateAssertionTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.RedundantAssertionTestSmellPytestInspection
import org.jetbrains.research.pynose.plugin.inspections.pytest.disabled.*
import org.jetbrains.research.pynose.plugin.inspections.unittest.*
import org.jetbrains.research.pynose.plugin.inspections.unittest.disabled.*
import org.jetbrains.research.pynose.plugin.inspections.universal.*

object HeadlessInspectionContainer {
    fun getPytestInspectionsFunctionLevel(): List<AbstractTestSmellInspection> {
        return listOf(
            ConditionalTestLogicTestSmellInspection(),
            EmptyTestTestSmellInspection(),
            ExceptionHandlingTestSmellInspection(),
            MagicNumberTestTestSmellPytestInspection(),
            RedundantAssertionTestSmellPytestInspection()
        )
    }

    fun getPytestInspectionsFileLaunchLevel(): List<AbstractTestSmellInspection> {
        return listOf(
            AssertionRouletteTestSmellPytestInspection(),
            DuplicateAssertionTestSmellPytestInspection(),
            ObscureInLineSetupTestSmellPytestInspection()
        )
    }

    fun getPytestInspectionsFileResultLevel(): List<AbstractTestSmellInspection> {
        return listOf(
            LackCohesionTestSmellPytestInspection(),
            TestMaverickTestSmellPytestInspection()
        )
    }

    fun getUnittestInspectionsFunctionResultLevel(): List<AbstractTestSmellInspection> {
        return listOf(
            AssertionRouletteTestSmellUnittestInspection(),
            ConditionalTestLogicTestSmellInspection(),
            ConstructorInitializationTestSmellUnittestInspection(),
            DuplicateAssertionTestSmellUnittestInspection(),
            EmptyTestTestSmellInspection(),
            ExceptionHandlingTestSmellInspection(),
            ObscureInLineSetupTestSmellUnittestInspection(),
            MagicNumberTestTestSmellUnittestInspection(),
            RedundantAssertionTestSmellUnittestInspection(),
            SuboptimalAssertTestSmellUnittestInspection()
        )
    }

    fun getUnittestInspectionsClassResultLevel(): List<AbstractTestSmellInspection> {
        return listOf(
            DefaultTestTestSmellUnittestInspection(),
            LackCohesionTestSmellUnittestInspection(),
            TestMaverickTestSmellUnittestInspection()
        )
    }

    fun getUniversalInspections(): List<AbstractTestSmellInspection> {
        return listOf(
            RedundantPrintTestSmellInspection(),
            SleepyTestTestSmellInspection()
        )
    }
}