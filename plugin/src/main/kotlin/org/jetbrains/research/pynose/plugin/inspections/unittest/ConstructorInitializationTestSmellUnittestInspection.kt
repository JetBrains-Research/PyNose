package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.quickfixes.unittest.ConstructorInitializationTestSmellQuickFix
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class ConstructorInitializationTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(ConstructorInitializationTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        fun registerConstructorInitialization(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.constructor.initialization.description"),
                ProblemHighlightType.WEAK_WARNING,
                ConstructorInitializationTestSmellQuickFix()
            )
        }
        return object : PyInspectionVisitor(holder, getContext(session)) {
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