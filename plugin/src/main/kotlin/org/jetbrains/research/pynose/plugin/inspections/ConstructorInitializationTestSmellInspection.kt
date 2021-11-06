package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementVisitor
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class ConstructorInitializationTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ConstructorInitializationTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

        fun registerConstructorInitialization(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.constructor.initialization.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    node.statementList.statements
                        .filterIsInstance<PyFunction>()
                        .filter { it.name == "__init__" }
                        .forEach {
                            registerConstructorInitialization(it.nameIdentifier!!)
                        }
                }
            }
        }
    }

}