package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ConditionalTestLogicTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ConditionalTestLogicTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PyElementVisitor {
        fun registerConditional(valueParam: PsiElement, offset: Int = 0, textLength: Int = valueParam.textLength) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.conditional.description"),
                ProblemHighlightType.WARNING,
                TextRange(offset, textLength)
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyIfStatement(ifStatement: PyIfStatement) {
                super.visitPyIfStatement(ifStatement)
                if (GeneralInspectionsUtils.redirectValidParentCheck(ifStatement)) {
                    registerConditional(ifStatement, 0, "if".length)
                }
            }

            override fun visitPyForStatement(forStatement: PyForStatement) {
                super.visitPyForStatement(forStatement)
                if (GeneralInspectionsUtils.redirectValidParentCheck(forStatement)) {
                    registerConditional(forStatement, 0, "for".length)
                }
            }

            override fun visitPyWhileStatement(whileStatement: PyWhileStatement) {
                super.visitPyWhileStatement(whileStatement)
                if (GeneralInspectionsUtils.redirectValidParentCheck(whileStatement)) {
                    registerConditional(whileStatement, 0, "while".length)
                }
            }

            override fun visitPyListCompExpression(listCompExpression: PyListCompExpression) {
                super.visitPyListCompExpression(listCompExpression)
                if (GeneralInspectionsUtils.redirectValidParentCheck(listCompExpression)) {
                    registerConditional(listCompExpression)
                }
            }

            override fun visitPySetCompExpression(setCompExpression: PySetCompExpression) {
                super.visitPySetCompExpression(setCompExpression)
                if (GeneralInspectionsUtils.redirectValidParentCheck(setCompExpression)) {
                    registerConditional(setCompExpression)
                }
            }

            override fun visitPyDictCompExpression(dictCompExpression: PyDictCompExpression) {
                super.visitPyDictCompExpression(dictCompExpression)
                if (GeneralInspectionsUtils.redirectValidParentCheck(dictCompExpression)) {
                    registerConditional(dictCompExpression)
                }
            }

            override fun visitPyGeneratorExpression(generatorExpression: PyGeneratorExpression) {
                super.visitPyGeneratorExpression(generatorExpression)
                if (GeneralInspectionsUtils.redirectValidParentCheck(generatorExpression)) {
                    registerConditional(generatorExpression)
                }
            }
        }
    }
}