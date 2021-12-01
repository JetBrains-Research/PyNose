package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import org.jetbrains.research.pynose.plugin.inspections.common.EmptyTestTestSmellInspectionVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode


class EmptyTestTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(EmptyTestTestSmellPytestInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        return if (PyNoseMode.getPyNosePytestMode()) {
            EmptyTestTestSmellInspectionVisitor(holder, session)
        } else {
            PsiElementVisitor.EMPTY_VISITOR
        }
    }
}