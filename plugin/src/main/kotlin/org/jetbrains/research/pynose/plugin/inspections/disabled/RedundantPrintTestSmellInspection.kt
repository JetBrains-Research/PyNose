package org.jetbrains.research.pynose.plugin.inspections.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.pyi.PyiFile
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class RedundantPrintTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(RedundantPrintTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        fun registerRedundantPrint(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.redundant.print.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val child = callExpression.callee as? PyReferenceExpression ?: return
                if (child.text != "print" || !GeneralInspectionsUtils.checkValidParent(callExpression)) {
                    return
                }
                val element = child.followAssignmentsChain(PyResolveContext.defaultContext()).element ?: return
                if (element.parent is PyiFile && (element.parent as PyiFile).name == "builtins.pyi" &&
                    element.parent.parent is PsiDirectory && (element.parent.parent as PsiDirectory).name == "stdlib"
                ) {
                    registerRedundantPrint(callExpression)
                }
            }
        }
    }
}