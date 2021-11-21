package org.jetbrains.research.pynose.plugin.quickfixes.unittest

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class SuboptimalAssertionTestSmellQuickFix : LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.suboptimal.message")
    }

    override fun applyFix(p0: Project, p1: ProblemDescriptor) {
        TODO("Not yet implemented")
    }
}