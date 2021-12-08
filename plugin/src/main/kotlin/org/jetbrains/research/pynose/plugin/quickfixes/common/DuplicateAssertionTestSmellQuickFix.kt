package org.jetbrains.research.pynose.plugin.quickfixes.common

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.python.psi.PyAssertStatement
import com.jetbrains.python.psi.PyCallExpression
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class DuplicateAssertionTestSmellQuickFix : LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.duplicate.message")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val assertion = descriptor.psiElement
        if (assertion is PyCallExpression) {
            assertion.parent.delete()
        } else if (assertion is PyAssertStatement) {
            assertion.delete()
        }
    }
}