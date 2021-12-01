package org.jetbrains.research.pynose.plugin.inspections.common

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.quickfixes.common.DuplicateAssertionTestSmellQuickFix
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class DuplicateAssertionTestSmellVisitor(holder: ProblemsHolder?, session: LocalInspectionToolSession) :
    PyInspectionVisitor(holder, session) {

    protected val assertCalls: MutableSet<Pair<String, PyFunction>> = mutableSetOf()
    protected val assertStatements: MutableSet<Pair<String, PyFunction>> = mutableSetOf()

    protected fun registerDuplicate(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.duplicate.description"),
            ProblemHighlightType.WARNING,
            DuplicateAssertionTestSmellQuickFix()
        )
    }

    protected fun processPyAssertStatement(assertStatement: PyAssertStatement, testMethod: PyFunction) {
        val assertArgs = assertStatement.arguments
        if (assertArgs.isEmpty()) {
            return
        }
        val assertStatementBody = assertArgs[0].text
        if (assertStatements.contains(Pair(assertStatementBody, testMethod))) {
            registerDuplicate(assertStatement)
        } else {
            assertStatements.add(Pair(assertStatementBody, testMethod))
        }
    }
}
