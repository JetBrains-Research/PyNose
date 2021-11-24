package org.jetbrains.research.pynose.plugin.inspections.unittest

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.PythonUiService
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

open class DefaultTestTestSmellUnittestInspection : PyInspection() {
    private val LOG = Logger.getInstance(DefaultTestTestSmellUnittestInspection::class.java)

    override fun buildVisitor(
            holder: ProblemsHolder,
            isOnTheFly: Boolean,
            session: LocalInspectionToolSession
    ): PsiElementVisitor {

        fun registerDefault(valueParam: PsiElement, quickFix: LocalQuickFix?) {
            holder.registerProblem(
                    valueParam,
                    TestSmellBundle.message("inspections.default.description"),
                    ProblemHighlightType.WARNING,
                    quickFix
            )
        }

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node) && node.name == "MyTestCase") {
                    // todo: 'com.jetbrains.python.PythonUiService' is marked unstable with @ApiStatus.Experimental
                    val quickFix = PythonUiService.getInstance().createPyRenameElementQuickFix(node)
                    registerDefault(node.nameIdentifier!!, quickFix)
                }
            }
        }
    }
}