package org.jetbrains.research.pynose.plugin.inspections.universal

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.inspections.TestRunnerServiceFacade
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ConditionalTestLogicTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ConditionalTestLogicTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        val testRunner = holder.project.service<TestRunnerServiceFacade>().getConfiguredTestRunner(holder.file)
        if (testRunner == "pytest" || testRunner == "Unittests") {
            return object : PyInspectionVisitor(holder, session) {

                private fun registerConditional(
                    valueParam: PsiElement,
                    offset: Int = 0,
                    textLength: Int = valueParam.textLength
                ) {
                    holder.registerProblem(
                        valueParam,
                        TestSmellBundle.message("inspections.conditional.description"),
                        ProblemHighlightType.WEAK_WARNING,
                        TextRange(offset, textLength)
                    )
                }

                override fun visitPyIfStatement(ifStatement: PyIfStatement) {
                    super.visitPyIfStatement(ifStatement)
                    if (GeneralInspectionsUtils.checkValidParent(ifStatement)) {
                        registerConditional(ifStatement, 0, "if".length)
                    }
                }

                override fun visitPyForStatement(forStatement: PyForStatement) {
                    super.visitPyForStatement(forStatement)
                    if (GeneralInspectionsUtils.checkValidParent(forStatement)) {
                        registerConditional(forStatement, 0, "for".length)
                    }
                }

                override fun visitPyWhileStatement(whileStatement: PyWhileStatement) {
                    super.visitPyWhileStatement(whileStatement)
                    if (GeneralInspectionsUtils.checkValidParent(whileStatement)) {
                        registerConditional(whileStatement, 0, "while".length)
                    }
                }

                override fun visitPyListCompExpression(listCompExpression: PyListCompExpression) {
                    super.visitPyListCompExpression(listCompExpression)
                    if (GeneralInspectionsUtils.checkValidParent(listCompExpression)) {
                        registerConditional(listCompExpression)
                    }
                }

                override fun visitPySetCompExpression(setCompExpression: PySetCompExpression) {
                    super.visitPySetCompExpression(setCompExpression)
                    if (GeneralInspectionsUtils.checkValidParent(setCompExpression)) {
                        registerConditional(setCompExpression)
                    }
                }

                override fun visitPyDictCompExpression(dictCompExpression: PyDictCompExpression) {
                    super.visitPyDictCompExpression(dictCompExpression)
                    if (GeneralInspectionsUtils.checkValidParent(dictCompExpression)) {
                        registerConditional(dictCompExpression)
                    }
                }

                override fun visitPyGeneratorExpression(generatorExpression: PyGeneratorExpression) {
                    super.visitPyGeneratorExpression(generatorExpression)
                    if (GeneralInspectionsUtils.checkValidParent(generatorExpression)) {
                        registerConditional(generatorExpression)
                    }
                }
            }
        } else {
            return PsiElementVisitor.EMPTY_VISITOR
        }
    }
}