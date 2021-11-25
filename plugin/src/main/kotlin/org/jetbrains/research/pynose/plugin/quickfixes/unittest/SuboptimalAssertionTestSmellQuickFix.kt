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
        "LEQ" to "<=",
        "LE" to "<",
        "GEQ" to ">=",
        "GE" to ">",
        "EQ" to "==",
        "NE" to "!=",
        "IS" to "is",
        "IS NOT" to "is not",
        "IN" to "in",
        "NOT IN" to "not in"
    )

    // todo: only one suboptimal assert can be fixed

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val assertCall = descriptor.psiElement as PyCallExpression
        val callee = assertCall.callee ?: return
        val elementGenerator = PyElementGenerator.getInstance(project)
        if (callee.name == "assertTrue") {
            val binaryExpression = assertCall.arguments[0]
            val children = binaryExpression.children
            binaryExpression.descendants().forEach { ss ->
                if (ss.text.equals(relationTypes["NE"])) {
                    val newExpressionText = "self.assertNotEqual(" + children[0].text + "," + children[1].text + ")"
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