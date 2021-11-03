package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class MagicNumberTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(MagicNumberTestTestSmellInspection::class.java)

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PyElementVisitor {

        fun registerMagicNumber(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.magic.number.description"),
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
                val child = callExpression.firstChild
                if (child !is PyReferenceExpression || !PyNoseUtils.isCallAssertMethod(child)
                    || !checkParent(callExpression)
                ) {
                    return
                }
                if (callExpression.arguments.any { obj: PyExpression? ->
                        PyNumericLiteralExpression::class.java.isInstance(obj)
                                || (PyBinaryExpression::class.java.isInstance(obj)
                                && obj!!.children.any { child ->
                            PyNumericLiteralExpression::class.java.isInstance(child)
                        })
                    }) {
                    registerMagicNumber(callExpression)
                }
            }

            override fun visitPyAssertStatement(assertStatement: PyAssertStatement) {
                super.visitPyAssertStatement(assertStatement)
                val assertArgs = assertStatement.arguments
                if (assertArgs.isEmpty() || !checkParent(assertStatement)) {
                    return
                }
                if (assertArgs.any { obj: PyExpression? ->
                        PyNumericLiteralExpression::class.java.isInstance(obj)
                                || (PyBinaryExpression::class.java.isInstance(obj)
                                && obj!!.children.any { child ->
                            PyNumericLiteralExpression::class.java.isInstance(child)
                        })
                    }) {
                    registerMagicNumber(assertStatement)
                }
            }
        }
    }
}