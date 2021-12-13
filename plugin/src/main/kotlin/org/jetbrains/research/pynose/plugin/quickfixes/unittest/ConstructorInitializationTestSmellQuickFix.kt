package org.jetbrains.research.pynose.plugin.quickfixes.unittest

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.impl.PyFunctionImpl
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle


class ConstructorInitializationTestSmellQuickFix : LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.constructor.message")
    }

    private val initRegex: Regex by lazy { "^def __init__\\(.*?\\):".toRegex() }
    private val superInitRegex: Regex by lazy { "\n[ ]*super\\(.*?.\\)\\.__init__\\(.*?\\)\n".toRegex() }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val problemElement = descriptor.psiElement
        val body = problemElement.parent.text
            .replace(initRegex, "")
            .replace(superInitRegex, "")
        val newExpressionText = "def setUp(self):\n$body"
        val elementGenerator: PyElementGenerator = PyElementGenerator.getInstance(project)
        problemElement.parent.replace(
            elementGenerator.createFromText(
                LanguageLevel.forElement(problemElement),
                PyFunctionImpl::class.java,
                newExpressionText
            )
        )
    }
}