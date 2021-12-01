package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.EmptyTestTestSmellInspectionVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode


class EmptyTestTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(EmptyTestTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return EmptyTestTestSmellInspectionVisitor(holder, session)
    }
}