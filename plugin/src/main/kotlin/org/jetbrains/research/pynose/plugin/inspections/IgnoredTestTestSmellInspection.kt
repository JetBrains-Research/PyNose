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
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyDecorator
import com.jetbrains.python.psi.PyElement
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

open class IgnoredTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(IgnoredTestTestSmellInspection::class.java)
    private val decoratorText = "@unittest.skip"

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        fun registerIgnored(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.ignored.description"),
                ProblemHighlightType.WARNING
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                        .forEach { testMethod ->
                            PsiTreeUtil
                                .collectElements(testMethod) { element -> (element is PyDecorator) }
                                .forEach { target -> processPyDecorator(target as PyDecorator) }
                        }
                    node.decoratorList?.decorators?.forEach { decorator ->
                        if (decorator.text.startsWith(decoratorText)) {
                            registerIgnored(node.nameIdentifier!!)
                        }
                    }
                }
            }

            private fun processPyDecorator(decorator: PyDecorator) {
                if (!decorator.text.startsWith(decoratorText)) {
                    decorator.children.forEach { child -> visitPyElement(child!! as PyElement) }
                    return
                }
                if (decorator.target != null) {
                    registerIgnored(decorator.target!!.nameIdentifier!!)
                }
            }
        }
    }
}