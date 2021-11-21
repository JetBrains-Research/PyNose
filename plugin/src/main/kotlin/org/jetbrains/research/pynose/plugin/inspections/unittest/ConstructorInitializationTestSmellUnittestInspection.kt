package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.quickfixes.unittest.ConstructorInitializationTestSmellQuickFix
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class ConstructorInitializationTestSmellUnittestInspection : PyInspection() {
    private val LOG = Logger.getInstance(ConstructorInitializationTestSmellUnittestInspection::class.java)

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {

        fun registerConstructorInitialization(valueParam: PsiElement) {
            holder.registerProblem(
                    valueParam,
                    TestSmellBundle.message("inspections.constructor.initialization.description"),
                    ProblemHighlightType.WARNING,
                    ConstructorInitializationTestSmellQuickFix()
            )
        }

        if (PyNoseMode.getPyNoseUnittestMode()) {
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
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}