package org.jetbrains.research.pynose.plugin.quickfixes.common

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class SleepyTestTestSmellQuickFix : LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.sleepy.message")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        descriptor.psiElement.parent.delete()
    }
}