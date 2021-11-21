package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class ObscureInLineSetupTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ObscureInLineSetupTestSmellInspection::class.java)
    private val testMethodLocalVarCount: MutableMap<PyFunction, MutableSet<String?>> = mutableMapOf()

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        fun registerObscureInLineSetup(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.obscure.setup.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                        .forEach { testMethod ->
                            visitPyElement(testMethod)
                            PsiTreeUtil
                                .collectElements(testMethod) { element -> (element is PyAssignmentStatement) }
                                .forEach { target ->
                                    processPyAssignmentStatement(
                                        target as PyAssignmentStatement,
                                        testMethod
                                    )
                                }
                        }
                    testMethodLocalVarCount.keys
                        .filter { x -> testMethodLocalVarCount[x]!!.size > 10 }
                        .forEach { x ->
                            registerObscureInLineSetup(x.nameIdentifier!!)
                        }
                    testMethodLocalVarCount.clear()
                }
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
        }
    }
}