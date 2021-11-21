package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class SuboptimalAssertTestSmellUnittestInspection : PyInspection() {
    private val LOG = Logger.getInstance(SuboptimalAssertTestSmellUnittestInspection::class.java)

    private val CHECKERS: MutableList<(PyCallExpression) -> Boolean> = mutableListOf(
        this::checkAssertTrueFalseRelatedSmell,
        this::checkAssertEqualNotEqualIsIsNotRelatedSmell
    )

    private fun checkAssertTrueFalseRelatedSmell(assertCall: PyCallExpression): Boolean {
        var callee: PyExpression
        if (assertCall.callee.also { callee = it!! } == null
            || callee.name != "assertTrue" && callee.name != "assertFalse") {
            return false
        }
        val args = assertCall.arguments
        return args.isNotEmpty() && args[0] is PyBinaryExpression
    }

    private fun checkAssertEqualNotEqualIsIsNotRelatedSmell(assertCall: PyCallExpression): Boolean {
        var callee: PyExpression
        if (assertCall.callee.also { callee = it!! } == null ||
            (callee.name != "assertEqual"
                    && callee.name != "assertNotEqual"
                    && callee.name != "assertIs"
                    && callee.name != "assertIsNot")
        ) {
            return false
        }
        val args = assertCall.arguments
        return args.size >= 2 && args
            .any { arg: PyExpression? -> arg is PyBoolLiteralExpression || arg is PyNoneLiteralExpression }
    }


    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        fun registerSuboptimal(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.suboptimal.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                if (!UnittestInspectionsUtils.isValidUnittestParent(callExpression)) {
                    return
                }
                if (CHECKERS.any { checker -> checker(callExpression) }) {
                    registerSuboptimal(callExpression)
                }
            }
        }
    }

}