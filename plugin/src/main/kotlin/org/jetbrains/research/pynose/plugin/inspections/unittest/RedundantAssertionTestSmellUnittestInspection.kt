package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyReferenceExpression
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.RedundantAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class RedundantAssertionTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(RedundantAssertionTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : RedundantAssertionTestSmellVisitor(holder, session) {
            // todo: assertTrue(4 < 4) is not detected
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                val child = callExpression.callee
                if (child !is PyReferenceExpression || !UnittestInspectionsUtils.isUnittestCallAssertMethod(child)
                    || !UnittestInspectionsUtils.isValidUnittestParent(callExpression)
                ) {
                    return
                }
                val argList = callExpression.getArguments(null)
                if (UnittestInspectionsUtils.ASSERT_METHOD_ONE_PARAM.containsKey(child.name)) {
                    if (argList[0].text == UnittestInspectionsUtils.ASSERT_METHOD_ONE_PARAM[child.name]) {
                        registerRedundant(callExpression)
                    }
                } else if (UnittestInspectionsUtils.ASSERT_METHOD_TWO_PARAMS.contains(child.name)) {
                    if (argList[0].text == argList[1].text) {
                        registerRedundant(callExpression)
                    }
                }
            }
        }
    }
}