package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyElementVisitor
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyPassStatement
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class EmptyTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(EmptyTestTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PyElementVisitor {

        fun registerEmpty(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.empty.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyFunction(testMethod: PyFunction) {
                super.visitPyFunction(testMethod)
                val statements = testMethod.statementList.statements
                if (statements.size == 1 && statements[0] is PyPassStatement
                    && GeneralInspectionsUtils.redirectValidMethodCheck(testMethod)
                ) {
                    registerEmpty(testMethod.nameIdentifier!!)
                }
            }
        }
    }
}