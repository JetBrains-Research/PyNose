package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyRaiseStatement
import com.jetbrains.python.psi.PyTryExceptStatement
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils

abstract class ExceptionHandlingTestSmellVisitor(holder: ProblemsHolder?, session: LocalInspectionToolSession) : PyInspectionVisitor(holder, session) {

    abstract fun registerException(valueParam: PsiElement, offset: Int = 0, textLength: Int)

    override fun visitPyTryExceptStatement(tryExceptStatement: PyTryExceptStatement) {
        super.visitPyTryExceptStatement(tryExceptStatement)
        if (GeneralInspectionsUtils.redirectValidParentCheck(tryExceptStatement)) {
            registerException(tryExceptStatement, 0, "try".length)
        }
    }

    override fun visitPyRaiseStatement(raiseStatement: PyRaiseStatement) {
        super.visitPyRaiseStatement(raiseStatement)
        if (GeneralInspectionsUtils.redirectValidParentCheck(raiseStatement)) {
            registerException(raiseStatement, 0, "raise".length)
        }
    }
}