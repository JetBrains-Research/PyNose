package org.jetbrains.research.pynose.plugin.inspections.universal

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyExpressionStatement
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.pyi.PyiFile
import org.jetbrains.research.pynose.plugin.quickfixes.common.SleepyTestTestSmellQuickFix
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class SleepyTestTestSmellInspection : AbstractUniversalTestSmellInspection() {
    private val LOG = Logger.getInstance(SleepyTestTestSmellInspection::class.java)
    override val inspectionName: String = "Sleepy test"

    override fun buildUniversalVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : PyInspectionVisitor(holder, getContext(session)) {

            private fun registerSleepy(valueParam: PsiElement) {
                holder.registerProblem(
                    valueParam,
                    TestSmellBundle.message("inspections.sleepy.description"),
                    ProblemHighlightType.WEAK_WARNING,
                    SleepyTestTestSmellQuickFix()
                )
            }

            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                if (!GeneralInspectionsUtils.checkValidParent(callExpression)) {
                    return
                }
                if (callExpression.callee !is PyReferenceExpression) {
                    super.visitPyElement(callExpression)
                    return
                }
                val callExprRef = callExpression.callee as? PyReferenceExpression ?: return
                val element = callExprRef.followAssignmentsChain(resolveContext).element ?: return
                if (element !is PyFunction || element.name != "sleep") {
                    super.visitPyElement(callExpression)
                    return
                }
                if (element.parent !is PyiFile || (element.parent as PyiFile).name != "time.pyi") {
                    super.visitPyElement(callExpression)
                    return
                }

                val parent = PsiTreeUtil.findFirstParent(callExpression, true) { it is PyExpressionStatement }
                    ?: return
                if (parent.lastChild !is PsiComment) {
                    registerSleepy(callExpression)
                }
            }
        }
    }
}