package org.jetbrains.research.pynose.plugin.inspections.unittest

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
import com.jetbrains.python.psi.PyStringLiteralExpression
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

open class IgnoredTestTestSmellUnittestInspection : PyInspection() {
    private val LOG = Logger.getInstance(IgnoredTestTestSmellUnittestInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        if (PyNoseMode.getPyNoseUnittestMode()) {
            return object : PyInspectionVisitor(holder, session) {

                private val decoratorText = "@unittest.skip"

                private fun registerIgnored(valueParam: PsiElement) {
                    holder.registerProblem(
                        valueParam,
                        TestSmellBundle.message("inspections.ignored.description"),
                        ProblemHighlightType.WARNING
                    )
                }

                private fun checkArgs(decorator: PyDecorator): Boolean {
                    val args = decorator.argumentList?.arguments
                    val containsString = args?.filterIsInstance<PyStringLiteralExpression>()?.isNotEmpty()
                    return args?.isEmpty() == true || containsString == false
                }

                override fun visitPyClass(pyClass: PyClass) {
                    super.visitPyClass(pyClass)
                    if (UnittestInspectionsUtils.isValidUnittestCase(pyClass)) {
                        UnittestInspectionsUtils.gatherUnittestTestMethods(pyClass)
                            .forEach { testMethod ->
                                PsiTreeUtil
                                    .collectElements(testMethod) { element -> (element is PyDecorator) }
                                    .forEach { target -> processPyDecorator(target as PyDecorator) }
                            }
                        pyClass.decoratorList?.decorators?.forEach { decorator ->
                            if (decorator.text.startsWith(decoratorText) && checkArgs(decorator)) {
                                registerIgnored(pyClass.nameIdentifier!!)
                            }
                        }
                    }
                }

                private fun processPyDecorator(decorator: PyDecorator) {
                    if (decorator.target != null && checkArgs(decorator) && decorator.text.startsWith(decoratorText)) {
                        registerIgnored(decorator.target!!.nameIdentifier!!)
                    }
                }
            }
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}