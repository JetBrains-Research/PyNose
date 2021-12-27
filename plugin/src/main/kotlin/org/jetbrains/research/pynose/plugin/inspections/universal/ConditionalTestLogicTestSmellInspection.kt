package org.jetbrains.research.pynose.plugin.inspections.universal

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class ConditionalTestLogicTestSmellInspection : AbstractUniversalTestSmellInspection() {
    private val LOG = Logger.getInstance(ConditionalTestLogicTestSmellInspection::class.java)

    override fun buildUniversalVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession
    ): PyInspectionVisitor {
        return object : PyInspectionVisitor(holder, getContext(session)) {
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

            private fun checkAssertionInConditionalBlock(element: PsiElement): Boolean {
                if (PsiTreeUtil.collectElements(element) { it is PyAssertStatement }
                        .map { it as PyAssertStatement }.isNotEmpty()) {
                    return true
                }
                PsiTreeUtil.collectElements(element) { it is PyCallExpression }
                    .map { it as PyCallExpression }
                    .forEach { callExpression ->
                        val callee = callExpression.callee
                        if (callee is PyReferenceExpression
                            && UnittestInspectionsUtils.isUnittestCallAssertMethod(callee)
                            && UnittestInspectionsUtils.isValidUnittestParent(callExpression)
                        ) {
                            return true
                        }
                    }
                return false
            }

            override fun visitPyIfStatement(ifStatement: PyIfStatement) {
                super.visitPyIfStatement(ifStatement)
                if (GeneralInspectionsUtils.checkValidParent(ifStatement)
                    && checkAssertionInConditionalBlock(ifStatement)
                ) {
                    registerConditional(ifStatement, 0, "if".length)
                }
            }

            override fun visitPyForStatement(forStatement: PyForStatement) {
                super.visitPyForStatement(forStatement)
                if (GeneralInspectionsUtils.checkValidParent(forStatement)
                    && checkAssertionInConditionalBlock(forStatement)) {
                    registerConditional(forStatement, 0, "for".length)
                }
            }

            override fun visitPyWhileStatement(whileStatement: PyWhileStatement) {
                super.visitPyWhileStatement(whileStatement)
                if (GeneralInspectionsUtils.checkValidParent(whileStatement)
                    && checkAssertionInConditionalBlock(whileStatement)) {
                    registerConditional(whileStatement, 0, "while".length)
                }
            }

            override fun visitPyListCompExpression(listCompExpression: PyListCompExpression) {
                super.visitPyListCompExpression(listCompExpression)
                if (GeneralInspectionsUtils.checkValidParent(listCompExpression)
                    && checkAssertionInConditionalBlock(listCompExpression)) {
                    registerConditional(listCompExpression)
                }
            }

            override fun visitPySetCompExpression(setCompExpression: PySetCompExpression) {
                super.visitPySetCompExpression(setCompExpression)
                if (GeneralInspectionsUtils.checkValidParent(setCompExpression)
                    && checkAssertionInConditionalBlock(setCompExpression)) {
                    registerConditional(setCompExpression)
                }
            }

            override fun visitPyDictCompExpression(dictCompExpression: PyDictCompExpression) {
                super.visitPyDictCompExpression(dictCompExpression)
                if (GeneralInspectionsUtils.checkValidParent(dictCompExpression)
                    && checkAssertionInConditionalBlock(dictCompExpression)) {
                    registerConditional(dictCompExpression)
                }
            }

            override fun visitPyGeneratorExpression(generatorExpression: PyGeneratorExpression) {
                super.visitPyGeneratorExpression(generatorExpression)
                if (GeneralInspectionsUtils.checkValidParent(generatorExpression)
                    && checkAssertionInConditionalBlock(generatorExpression)) {
                    registerConditional(generatorExpression)
                }
            }
        }
    }
}