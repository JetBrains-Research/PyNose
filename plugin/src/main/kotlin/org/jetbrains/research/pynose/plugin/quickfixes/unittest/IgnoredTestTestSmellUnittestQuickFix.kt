package org.jetbrains.research.pynose.plugin.quickfixes.unittest

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyDecorator
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.impl.PyDecoratorImpl
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class IgnoredTestTestSmellUnittestQuickFix(private val isUnittestMode: Boolean) : LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.ignored.message")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val decorator = descriptor.psiElement as PyDecorator
        val hasArgs = decorator.argumentList?.arguments?.filterIsInstance<PyStringLiteralExpression>()?.isNotEmpty()
        val elementGenerator = PyElementGenerator.getInstance(project)
        var newExpressionText = ""
        if (isUnittestMode) {
            if (hasArgs!!) {
                newExpressionText = "${decorator.text.subSequence(0, decorator.text.length - 1)}" + ", " + "\"reason\")"
            }
        }
        decorator.replace(
            elementGenerator.createFromText(
                LanguageLevel.forElement(decorator),
                PyDecoratorImpl::class.java, newExpressionText
            )
        )
    }
}