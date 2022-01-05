package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.inspections.PyInspectionVisitor
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.MagicNumberTestSmellVisitor

class MagicNumberTestTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(MagicNumberTestTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyInspectionVisitor {
        return MagicNumberTestSmellVisitor(holder, session)
    }
}