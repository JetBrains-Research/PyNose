package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import kotlin.reflect.KFunction1

class SuboptimalAssertTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(SuboptimalAssertTestSmellInspection::class.java)
    
    private val CHECKERS: MutableList<KFunction1<PyCallExpression, Boolean>> = mutableListOf(
        this::checkAssertTrueFalseRelatedSmell,
        this::checkAssertEqualNotEqualIsIsNotRelatedSmell
    )

    private fun checkAssertTrueFalseRelatedSmell(assertCall: PyCallExpression): Boolean {
        var callee: PyExpression
        if (assertCall.callee.also { callee = it!! } == null) {
            return false
        }
        if (callee.name != "assertTrue" && callee.name != "assertFalse") {
            return false
        }
        val args = assertCall.arguments
        return args.isNotEmpty() && args[0] is PyBinaryExpression
    }

    private fun checkAssertEqualNotEqualIsIsNotRelatedSmell(assertCall: PyCallExpression): Boolean {
        var callee: PyExpression
        if (assertCall.callee.also { callee = it!! } == null) {
            return false
        }
        if (callee.name != "assertEqual" &&
            callee.name != "assertNotEqual" &&
            callee.name != "assertIs" &&
            callee.name != "assertIsNot"
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
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

        fun registerSuboptimal(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.suboptimal.description"),
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

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyCallExpression(callExpression: PyCallExpression) {
                super.visitPyCallExpression(callExpression)
                if (!checkParent(callExpression)) {
                    return
                }
                if (CHECKERS.stream().anyMatch { checker -> checker(callExpression) }) {
                    registerSuboptimal(callExpression)
                }
            }
        }
    }

}