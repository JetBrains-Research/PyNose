package org.jetbrains.research.pynose.plugin.inspections.universal

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection


abstract class AbstractUniversalTestSmellInspection : AbstractTestSmellInspection() {
    protected abstract fun buildUniversalVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession
    ): PsiElementVisitor

    override fun buildUnittestVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession
    ) = buildUniversalVisitor(holder, session)

    override fun buildPytestVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession
    ) = buildUniversalVisitor(holder, session)
}