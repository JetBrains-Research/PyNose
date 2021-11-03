package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.pyi.PyiFile
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class RedundantPrintTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(RedundantPrintTestSmellInspection::class.java)

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PyElementVisitor {

        fun registerRedundantPrint(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.redundant.print.description"),
                ProblemHighlightType.WARNING
            )
        }

        fun checkParent(element: PsiElement): Boolean {
            return (PyNoseUtils.isValidUnittestMethod(
                PsiTreeUtil.getParentOfType(
                    element,
                    PyFunction::class.java
                )
            )
                    )
        }

        return object : PyElementVisitor() {

            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val child = callExpression.firstChild as? PyReferenceExpression ?: return
                if (child.text != "print" || !checkParent(callExpression)) {
                    return
                }
                val e = child.followAssignmentsChain(PyResolveContext.defaultContext()).element ?: return
                if (e.parent is PyiFile && (e.parent as PyiFile).name == "builtins.pyi" &&
                    e.parent.parent is PsiDirectory && (e.parent.parent as PsiDirectory).name == "stdlib"
                ) {
                    registerRedundantPrint(callExpression)
                }
            }
        }
    }
}