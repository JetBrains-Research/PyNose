package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyLiteralExpression
import com.jetbrains.python.psi.PyReferenceExpression
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.RedundantAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class RedundantAssertionTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(RedundantAssertionTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : RedundantAssertionTestSmellVisitor(holder, session) {
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val callee = callExpression.callee ?: return
                if (callee !is PyReferenceExpression || !UnittestInspectionsUtils.isUnittestCallAssertMethod(callee)
                    || !UnittestInspectionsUtils.isValidUnittestParent(callExpression)
                ) {
                    return
                }
                val argList = callExpression.getArguments(null)
                if (UnittestInspectionsUtils.ASSERT_METHOD_ONE_PARAM.containsKey(callee.name)
                    && processAssertionArgs(callExpression, argList)) {
                    return
                } else if (UnittestInspectionsUtils.ASSERT_METHOD_TWO_PARAMS.contains(callee.name)) {
                    if (argList.size >= 2 && (PyLiteralExpression::class.java.isAssignableFrom(argList[0]::class.java)
                                && PyLiteralExpression::class.java.isAssignableFrom(argList[1]::class.java))) {
                        registerRedundant(callExpression)
                    }
                }
            }
        }
    }
}