package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementVisitor
import org.jetbrains.research.pynose.core.PyNoseUtils

open class DefaultTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(this::class.java)

    companion object {
        const val warningDescription = "Consider changing the name of your test suite to a non-default one" +
                " to better reflect its content"
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PyElementVisitor() {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (PyNoseUtils.isValidUnittestCase(node) && node.name == "MyTestCase") {
                    holder.registerProblem(
                        node.nameIdentifier!!,
                        warningDescription,
                        ProblemHighlightType.WARNING
                    )
                }
            }
        }
    }

}