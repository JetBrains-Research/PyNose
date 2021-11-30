package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.inspections.common.MagicNumberTestSmellVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class MagicNumberTestTestSmellUnittestInspection : PyInspection() {
    private val LOG = Logger.getInstance(MagicNumberTestTestSmellUnittestInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        if (PyNoseMode.getPyNoseUnittestMode()) {
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
                            obj is PyNumericLiteralExpression ||
                                    (obj is PyBinaryExpression
                                            && obj.children.any { child ->
                                        child is PyNumericLiteralExpression
                                    })
                        }) {
                        registerMagicNumber(callExpression)
                    }
                }
            }
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}