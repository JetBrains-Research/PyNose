package org.jetbrains.research.pynose.plugin.inspections.universal

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyPassStatement
import org.jetbrains.research.pynose.plugin.quickfixes.common.EmptyTestTestSmellQuickFix
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle


class EmptyTestTestSmellInspection : AbstractUniversalTestSmellInspection() {
    private val LOG = Logger.getInstance(EmptyTestTestSmellInspection::class.java)

    override fun buildUniversalVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyInspectionVisitor {
        return object : PyInspectionVisitor(holder, getContext(session)) {
            private fun registerEmpty(valueParam: PsiElement) {
                holder.registerProblem(
                    valueParam,
                    TestSmellBundle.message("inspections.empty.description"),
                    ProblemHighlightType.WARNING,
                    EmptyTestTestSmellQuickFix()
                )
            }

            override fun visitPyFunction(testMethod: PyFunction) {
                super.visitPyFunction(testMethod)
                val statements = testMethod.statementList.statements
                if (statements.size == 1 && statements[0] is PyPassStatement
                    && GeneralInspectionsUtils.checkValidMethod(testMethod)
                ) {
                    registerEmpty(testMethod.nameIdentifier!!)
                }
            }
        }
    }
}