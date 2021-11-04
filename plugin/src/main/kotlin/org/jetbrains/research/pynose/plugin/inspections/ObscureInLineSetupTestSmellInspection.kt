package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementVisitor
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ObscureInLineSetupTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ObscureInLineSetupTestSmellInspection::class.java)
    private val testMethodLocalVarCount: MutableMap<PyFunction, MutableSet<String?>> = mutableMapOf()

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

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
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    PyNoseUtils.gatherTestMethods(node).forEach { testMethod ->
                        visitPyElement(testMethod)
                    }
                    testMethodLocalVarCount.keys.stream().filter { x -> testMethodLocalVarCount[x]!!.size > 10 }
                        .forEach { x ->
                            registerObscureInLineSetup(x.nameIdentifier!!)
                        }
                    testMethodLocalVarCount.clear()
                }
            }

            override fun visitPyAssignmentStatement(assignmentStatement: PyAssignmentStatement) {
                super.visitPyAssignmentStatement(assignmentStatement)
                val testMethod = PsiTreeUtil.getParentOfType(assignmentStatement, PyFunction::class.java)
                if (!PyNoseUtils.isValidUnittestMethod(testMethod)) {
                    return
                }
                if (testMethodLocalVarCount[testMethod] == null) {
                    testMethodLocalVarCount[testMethod!!] = mutableSetOf()
                }
                val localVars: MutableSet<String?>? = testMethodLocalVarCount[testMethod]
                for (target in assignmentStatement.targets) {
                    if (target.children.isEmpty()) {
                        localVars!!.add(target.name)
                    }
                }
            }
        }
    }
}