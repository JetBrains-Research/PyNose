package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.SleepyTestTestSmellVisitor

class SleepyTestTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(SleepyTestTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return SleepyTestTestSmellVisitor(holder, session)
    }
}