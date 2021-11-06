package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.pyi.PyiFile
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class SleepyTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(SleepyTestTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

        fun registerSleepy(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.sleepy.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                if (!UnittestInspectionsUtils.isValidUnittestParent(callExpression)) {
                    return
                }
                if (callExpression.firstChild !is PyReferenceExpression) {
                    callExpression.children.forEach { child -> visitElement(child!!) }
                    return
                }
                val callExprRef = callExpression.firstChild as? PyReferenceExpression ?: return
                val element = callExprRef.followAssignmentsChain(PyResolveContext.defaultContext()).element ?: return
                if (element !is PyFunction || element.name != "sleep") {
                    callExpression.children.forEach { child -> visitElement(child!!) }
                    return
                }
                if (element.parent !is PyiFile || (element.parent as PyiFile).name != "time.pyi") {
                    callExpression.children.forEach { child -> visitElement(child!!) }
                    return
                }

                var parent = callExpression.parent
                while (parent !is PyExpressionStatement) {
                    parent = parent.parent
                }
                if (parent.getLastChild() !is PsiComment) {
                    registerSleepy(callExpression)
                }
            }

        }
    }
}