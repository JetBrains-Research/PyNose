package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.testing.TestRunnerService


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
        val module = ModuleUtilCore.findModuleForPsiElement(holder.file)
        return when (TestRunnerService.getInstance(module).projectConfiguration) {
            "pytest" -> buildPytestVisitor(holder, session)
            "Unittests" -> buildUnittestVisitor(holder, session)
            else -> PsiElementVisitor.EMPTY_VISITOR
        }
    }
}