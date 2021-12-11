package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.RedundantAssertionTestSmellVisitor

class RedundantAssertionTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(RedundantAssertionTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : RedundantAssertionTestSmellVisitor(holder, session) {}
    }
}