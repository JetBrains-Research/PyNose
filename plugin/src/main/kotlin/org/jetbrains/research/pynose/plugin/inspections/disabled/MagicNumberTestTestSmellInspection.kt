package org.jetbrains.research.pynose.plugin.inspections.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class MagicNumberTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(MagicNumberTestTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        fun registerMagicNumber(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.magic.number.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val child = callExpression.callee
                if (child !is PyReferenceExpression ||
                    !UnittestInspectionsUtils.isUnittestCallAssertMethod(child)
                    || !GeneralInspectionsUtils.checkValidParent(callExpression)
                ) {
                    return
                }
                if (callExpression.arguments.any { obj: PyExpression? ->
                        obj is PyNumericLiteralExpression ||
                                (obj is PyBinaryExpression
                                        && obj.children.any { child ->
                                    child is PyNumericLiteralExpression
                                })
                    }) {
                    registerMagicNumber(callExpression)
                }
            }

            override fun visitPyAssertStatement(assertStatement: PyAssertStatement) {
                super.visitPyAssertStatement(assertStatement)
                val assertArgs = assertStatement.arguments
                if (assertArgs.isEmpty() || !GeneralInspectionsUtils.checkValidParent(assertStatement)) {
                    return
                }
                if (assertArgs.any { obj: PyExpression? ->
                        obj is PyNumericLiteralExpression
                                || (obj is PyBinaryExpression
                                && obj.children.any { child ->
                            child is PyNumericLiteralExpression
                        })
                    }) {
                    registerMagicNumber(assertStatement)
                }
            }
        }
    }
}