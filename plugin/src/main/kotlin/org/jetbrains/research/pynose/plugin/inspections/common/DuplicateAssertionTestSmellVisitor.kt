package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyWithItem
import org.jetbrains.research.pynose.plugin.quickfixes.common.DuplicateAssertionTestSmellQuickFix
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class DuplicateAssertionTestSmellVisitor(holder: ProblemsHolder?, session: LocalInspectionToolSession) :
    PyInspectionVisitor(holder, getContext(session)) {

    protected fun registerDuplicate(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.duplicate.description"),
            ProblemHighlightType.WARNING,
            if (valueParam.parent is PyWithItem) null else DuplicateAssertionTestSmellQuickFix()
        )
    }

    protected fun processPyAssertStatements(assertStatements: List<PyAssertStatement>) {
        val visitedStatements = HashSet<String>()
        for (assertStatement in assertStatements) {
            val assertArgs = assertStatement.arguments
            if (assertArgs.isEmpty()) {
                continue
            }
            val assertStatementBody = assertArgs[0].text
            if (assertStatementBody in visitedStatements) {
                registerDuplicate(assertStatement)
            } else {
                visitedStatements += assertStatementBody
            }
        }
    }
}
