package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode

abstract class AbstractTestSmellInspection : PyInspection() {

    open protected fun buildUnitTestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return PsiElementVisitor.EMPTY_VISITOR
    }

    open protected fun buildPyTestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return PsiElementVisitor.EMPTY_VISITOR
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return when {
            PyNoseMode.getPyNosePytestMode() -> buildPyTestVisitor(holder, session)
            PyNoseMode.getPyNoseUnittestMode() -> buildUnitTestVisitor(holder, session)
            else -> PsiElementVisitor.EMPTY_VISITOR
        }
    }
}