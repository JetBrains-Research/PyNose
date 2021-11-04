package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.*
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ConditionalTestLogicTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ConditionalTestLogicTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

        fun registerConditional(valueParam: PsiElement, offset: Int = 0, textLength: Int = valueParam.textLength) {
            holder.registerProblem(
                valueParam,
                TestSmellBundle.message("inspections.conditional.description"),
                ProblemHighlightType.WARNING,
                TextRange(offset, textLength)
            )
        }

        fun checkParent(element: PsiElement): Boolean {
            return (PyNoseUtils.isValidUnittestMethod(
                PsiTreeUtil.getParentOfType(
                    element,
                    PyFunction::class.java
                )
            )
                    )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyIfStatement(ifStatement: PyIfStatement) {
                super.visitPyIfStatement(ifStatement)
                if (checkParent(ifStatement)) {
                    registerConditional(ifStatement, 0, "if".length)
                }
            }

            override fun visitPyForStatement(forStatement: PyForStatement) {
                super.visitPyForStatement(forStatement)
                if (checkParent(forStatement)) {
                    registerConditional(forStatement, 0, "for".length)
                }
            }

            override fun visitPyWhileStatement(whileStatement: PyWhileStatement) {
                super.visitPyWhileStatement(whileStatement)
                if (checkParent(whileStatement)) {
                    registerConditional(whileStatement, 0, "while".length)
                }
            }

            override fun visitPyListCompExpression(listCompExpression: PyListCompExpression) {
                super.visitPyListCompExpression(listCompExpression)
                if (checkParent(listCompExpression)) {
                    registerConditional(listCompExpression)
                }
            }

            override fun visitPySetCompExpression(setCompExpression: PySetCompExpression) {
                super.visitPySetCompExpression(setCompExpression)
                if (checkParent(setCompExpression)) {
                    registerConditional(setCompExpression)
                }
            }

            override fun visitPyDictCompExpression(dictCompExpression: PyDictCompExpression) {
                super.visitPyDictCompExpression(dictCompExpression)
                if (checkParent(dictCompExpression)) {
                    registerConditional(dictCompExpression)
                }
            }

            override fun visitPyGeneratorExpression(generatorExpression: PyGeneratorExpression) {
                super.visitPyGeneratorExpression(generatorExpression)
                if (checkParent(generatorExpression)) {
                    registerConditional(generatorExpression)
                }
            }
        }
    }
}