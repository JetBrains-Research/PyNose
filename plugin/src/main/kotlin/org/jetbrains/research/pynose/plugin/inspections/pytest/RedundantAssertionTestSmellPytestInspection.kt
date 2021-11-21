package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyReferenceExpression
import org.jetbrains.research.pynose.plugin.inspections.common.RedundantAssertionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class RedundantAssertionTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(RedundantAssertionTestSmellPytestInspection::class.java)

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {

        if (PyNoseMode.getPyNoseUnittestMode()) {
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
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}