package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import org.jetbrains.research.pynose.plugin.inspections.common.RedundantAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode

class RedundantAssertionTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(RedundantAssertionTestSmellPytestInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        return if (PyNoseMode.getPyNosePytestMode()) {
            object : RedundantAssertionTestSmellVisitor(holder, session) {}
        } else {
            PsiElementVisitor.EMPTY_VISITOR
        }
    }
}