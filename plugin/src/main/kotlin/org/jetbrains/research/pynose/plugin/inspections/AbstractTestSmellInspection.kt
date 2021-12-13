package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection


abstract class AbstractTestSmellInspection : PyInspection() {

    protected open fun buildUnittestVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return PsiElementVisitor.EMPTY_VISITOR
    }

    protected open fun buildPytestVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return PsiElementVisitor.EMPTY_VISITOR
    }

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        return when (holder.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(holder.file)) {
            TestRunner.PYTEST -> buildPytestVisitor(holder, session)
            TestRunner.UNITTESTS -> buildUnittestVisitor(holder, session)
            else -> PsiElementVisitor.EMPTY_VISITOR
        }
    }
}