package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import org.jetbrains.research.pynose.plugin.inspections.common.ConditionalTestLogicTestSmellVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode

class ConditionalTestLogicTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(ConditionalTestLogicTestSmellPytestInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return if (PyNoseMode.getPyNosePytestMode()) {
            ConditionalTestLogicTestSmellVisitor(holder, session)
        } else {
            PsiElementVisitor.EMPTY_VISITOR
        }
    }
}