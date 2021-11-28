package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.impl.PyKeywordArgumentImpl
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

open class IgnoredTestTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(IgnoredTestTestSmellPytestInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {

        if (PyNoseMode.getPyNosePytestMode()) {
            return object : PyInspectionVisitor(holder, session) {

                private val decoratorText = setOf("@pytest.mark.skip", "@pytest.mark.xfail")

                private  fun registerIgnored(valueParam: PsiElement) {
                    holder.registerProblem(
                        valueParam,
                        TestSmellBundle.message("inspections.ignored.description"),
                        ProblemHighlightType.WARNING
                    )
                }

                private fun checkArgs(decorator: PyDecorator): Boolean {
                    val args = decorator.argumentList?.arguments
                    val containsString = args?.any { arg -> arg is PyKeywordArgumentImpl && arg.name == "reason" }
                    return args?.isEmpty() == true || containsString == false
                }

                override fun visitPyFile(file: PyFile) {
                    super.visitPyFile(file)
                    if (PytestInspectionsUtils.isValidPytestFile(file)) {
                        PytestInspectionsUtils.gatherValidPytestMethods(file)
                            .forEach { testMethod ->
                                PsiTreeUtil
                                    .collectElements(testMethod) { element -> (element is PyDecorator) }
                                    .forEach { target -> processPyDecorator(target as PyDecorator) }
                            }
                        PytestInspectionsUtils.gatherValidPytestClasses(file)
                            .forEach { pyClass ->
                                pyClass.decoratorList?.decorators?.forEach { decorator ->
                                    if (decoratorText.any { text -> decorator.text.startsWith(text) }
                                        && checkArgs(decorator)) {
                                        registerIgnored(pyClass.nameIdentifier!!)
                                    }
                                }
                            }
                    }
                }

                private fun processPyDecorator(decorator: PyDecorator) {
                    if (decorator.target != null && checkArgs(decorator)
                        && decoratorText.any { text -> decorator.text.startsWith(text) }
                    ) {
                        registerIgnored(decorator.target!!.nameIdentifier!!)
                    }
                }
            }
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}