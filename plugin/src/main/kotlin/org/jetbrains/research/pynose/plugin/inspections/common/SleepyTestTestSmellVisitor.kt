package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyExpressionStatement
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.pyi.PyiFile
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class SleepyTestTestSmellVisitor(
    holder: ProblemsHolder?,
    session: LocalInspectionToolSession
) : PyInspectionVisitor(holder, session) {

    private fun registerSleepy(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.sleepy.description"),
            ProblemHighlightType.WARNING
        )
    }

    override fun visitPyCallExpression(callExpression: PyCallExpression) {
        super.visitPyCallExpression(callExpression)
        if (!GeneralInspectionsUtils.checkValidParent(callExpression)) {
            return
        }
        if (callExpression.callee !is PyReferenceExpression) {
            super.visitPyElement(callExpression)
            return
        }
        val callExprRef = callExpression.callee as? PyReferenceExpression ?: return
        val element = callExprRef.followAssignmentsChain(PyResolveContext.defaultContext()).element ?: return
        if (element !is PyFunction || element.name != "sleep") {
            super.visitPyElement(callExpression)
            return
        }
        if (element.parent !is PyiFile || (element.parent as PyiFile).name != "time.pyi") {
            super.visitPyElement(callExpression)
            return
        }

        var parent = callExpression.parent
        while (parent !is PyExpressionStatement) {
            parent = parent.parent
        }
        if (parent.getLastChild() !is PsiComment) {
            registerSleepy(callExpression)
        }
    }
}