package org.jetbrains.research.pynose.plugin.quickfixes.unittest

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.descendants
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle


class SuboptimalAssertionTestSmellQuickFix : LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.suboptimal.message")
    }

    private val relationTypes = mapOf(
        "LE" to "<",
        "LEQ" to "<=",
        "GE" to ">",
        "GEQ" to ">=",
        "EQ" to "==",
        "NE" to "!=",
        "IS" to "is",
        "IS NOT" to "is not",
        "IN" to "in",
        "NOT IN" to "not in"
    )

    // todo: rn only assertTrue can be fixed, I'll add the other ones after review

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val assertCall = descriptor.psiElement as PyCallExpression
        val callee = assertCall.callee ?: return
        val elementGenerator = PyElementGenerator.getInstance(project)
        when (callee.name) {
            "assertTrue" -> {
                val binaryExpression = assertCall.arguments[0]
                val children = binaryExpression.children
                binaryExpression.descendants().forEach { des ->
                    // todo: currently fails for is not / not in (2 words in expression)
                    val assertionType = when (des.text) {
                        relationTypes["EQ"] -> "assertEqual"
                        relationTypes["NE"] -> "assertNotEqual"
                        relationTypes["IS"] -> "assertIs"
                        relationTypes["IS NOT"] -> "assertIsNot"
                        relationTypes["IN"] -> "assertIn"
                        relationTypes["NOT IN"] -> "assertNotIn"
                        relationTypes["LE"] -> "assertLess"
                        relationTypes["LEQ"] -> "assertLessEqual"
                        relationTypes["GE"] -> "assertGreater"
                        relationTypes["GEQ"] -> "assertGreaterEqual"
                        else -> null
                    }
                    if (assertionType != null) {
                        val newExpressionText =
                            "self.$assertionType(" + children[0].text + "," + children[1].text + ")"
                        assertCall.replace(
                            elementGenerator.createFromText(
                                LanguageLevel.forElement(assertCall),
                                PyExpressionStatementImpl::class.java, newExpressionText
                            )
                        )
                    }
                }
            }
        }
    }
}
