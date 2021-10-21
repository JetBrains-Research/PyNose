package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementVisitor
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.core.detectors.impl.ConstructorInitializationTestSmellDetector

class ConstructorInitializationTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ConstructorInitializationTestSmellDetector::class.java)

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PyElementVisitor() {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    node.statementList.statements
                        .filterIsInstance<PyFunction>()
                        .filter { it.name == "__init__" }
                        .forEach {
                            holder.registerProblem(
                                it.nameIdentifier!!,
                                "You can use the setUp() method to create the test fixture, " +
                                        "instead of initializing the constructor",
                                ProblemHighlightType.WARNING
                            )
                        }
                }
            }
        }
    }

}