package org.jetbrains.research.pynose.plugin.quickfixes.unittest

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ConstructorInitializationTestSmellQuickFix : LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.constructor.message")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        TODO("Not yet implemented")
    }
}