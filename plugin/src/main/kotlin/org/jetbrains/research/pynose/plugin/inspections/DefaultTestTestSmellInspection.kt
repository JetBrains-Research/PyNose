package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementVisitor
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

open class DefaultTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(DefaultTestTestSmellInspection::class.java)

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
        @NotNull session: LocalInspectionToolSession
    ): PyElementVisitor {

        return object : PyInspectionVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node) && node.name == "MyTestCase") {
                    holder.registerProblem(
                        node.nameIdentifier!!,
                        TestSmellBundle.message("inspections.default.description"),
                        ProblemHighlightType.WARNING
                    )
                }
            }
        }
    }

}