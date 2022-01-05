package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyReferenceExpression
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.RedundantAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class RedundantAssertionTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(RedundantAssertionTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyInspectionVisitor {
        return object : RedundantAssertionTestSmellVisitor(holder, session) {
            // todo: assertTrue(x < x) is not detected yet
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val callee = callExpression.callee ?: return
                if (callee !is PyReferenceExpression || !UnittestInspectionsUtils.isUnittestCallAssertMethod(callee)
                    || !UnittestInspectionsUtils.isValidUnittestParent(callExpression)
                ) {
                    return
                }
                val argList = callExpression.getArguments(null)
                if (UnittestInspectionsUtils.ASSERT_METHOD_ONE_PARAM.containsKey(callee.name)) {
                    if (argList.isNotEmpty() && argList[0].text == UnittestInspectionsUtils.ASSERT_METHOD_ONE_PARAM[callee.name]) {
                        registerRedundant(callExpression)
                    }
                } else if (UnittestInspectionsUtils.ASSERT_METHOD_TWO_PARAMS.contains(callee.name)) {
                    if (argList.size >= 2 && argList[0].text == argList[1].text) {
                        registerRedundant(callExpression)
                    }
                }
            }
        }
    }
}