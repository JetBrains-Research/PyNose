package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.MagicNumberTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class MagicNumberTestTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(MagicNumberTestTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {

        return object : MagicNumberTestSmellVisitor(holder, session) {
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val child = callExpression.callee
                if (child !is PyReferenceExpression ||
                    !UnittestInspectionsUtils.isUnittestCallAssertMethod(child)
                    || !UnittestInspectionsUtils.isValidUnittestParent(callExpression)
                ) {
                    return
                }
                if (callExpression.arguments.any { obj: PyExpression? ->
                        (obj is PyNumericLiteralExpression && !ignoredNumbers.contains(obj.text)) ||
                                (obj is PyBinaryExpression
                                        && obj.children.any { it is PyNumericLiteralExpression &&
                                            !ignoredNumbers.contains(it.text)
                                }) ||
                                (obj is PyParenthesizedExpression
                                        && obj.children.any {
                                    it is PyNumericLiteralExpression
                                            && !ignoredNumbers.contains(it.text)
                                })
                    }) {
                    registerMagicNumber(callExpression)
                }
            }
        }
    }
}