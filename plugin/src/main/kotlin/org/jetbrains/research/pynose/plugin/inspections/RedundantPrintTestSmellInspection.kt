package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.pyi.PyiFile
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class RedundantPrintTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(RedundantPrintTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

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
                val child = callExpression.firstChild as? PyReferenceExpression ?: return
                if (child.text != "print" || !UnittestInspectionsUtils.isValidUnittestParent(callExpression)) {
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