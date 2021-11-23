package org.jetbrains.research.pynose.plugin.inspections.pytest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.PythonUiService
import com.jetbrains.python.inspections.PyInspection
import org.jetbrains.research.pynose.plugin.inspections.common.EmptyTestTestSmellInspectionVisitor
import org.jetbrains.research.pynose.plugin.startup.PyNoseMode
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel


class EmptyTestTestSmellPytestInspection : PyInspection() {
    private val LOG = Logger.getInstance(EmptyTestTestSmellPytestInspection::class.java)
    var ignoreOverriddenFunctions = true

    override fun createOptionsPanel(): JComponent {
        val rootPanel = JPanel(BorderLayout())
        val uiService = PythonUiService.getInstance()
        val checkBox = uiService.createInspectionCheckBox(TestSmellBundle.message("quickfixes.empty.message"), this, "ignoreOverriddenFunctions")
        if (checkBox != null) {
            rootPanel.add(checkBox, BorderLayout.NORTH)
        }
        return rootPanel
    }

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {

        return if (PyNoseMode.getPyNosePytestMode()) {
            EmptyTestTestSmellInspectionVisitor(holder, session)
        } else {
            PsiElementVisitor.EMPTY_VISITOR
        }
    }
}