package org.jetbrains.research.pynose.plugin.inspections.common.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class ObscureInLineSetupTestSmellVisitor(
    holder: ProblemsHolder?,
    session: LocalInspectionToolSession
) : PyInspectionVisitor(holder, session) {

    private val testMethodLocalVarCount: MutableMap<PyFunction, MutableSet<String?>> = mutableMapOf()

    private fun registerObscureInLineSetup(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.obscure.setup.description"),
            ProblemHighlightType.WARNING
        )
    }

    private fun processPyAssignmentStatement(
        assignmentStatement: PyAssignmentStatement,
        testMethod: PyFunction
    ) {
        val localVars: MutableSet<String?> = testMethodLocalVarCount.getOrPut(testMethod) { mutableSetOf() }
        assignmentStatement.targets
            .filter { target -> target.children.isEmpty() }
            .forEach { target -> localVars.add(target.name) }
    }

    protected fun getObscureInLineSetup() {
        testMethodLocalVarCount.keys
            .filter { x -> testMethodLocalVarCount[x]!!.size > 10 }
            .forEach { x ->
                registerObscureInLineSetup(x.nameIdentifier!!)
            }
        testMethodLocalVarCount.clear()
    }

    protected fun processTestMethod(testMethod: PyFunction) {
        PsiTreeUtil
            .collectElements(testMethod) { element -> (element is PyAssignmentStatement) }
            .forEach { target ->
                processPyAssignmentStatement(
                    target as PyAssignmentStatement,
                    testMethod
                )
            }
    }

}