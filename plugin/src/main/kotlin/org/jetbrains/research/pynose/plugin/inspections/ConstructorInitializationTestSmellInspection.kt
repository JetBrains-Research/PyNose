package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementVisitor
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyStatement
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.core.detectors.impl.ConstructorInitializationTestSmellDetector
import java.util.*

class ConstructorInitializationTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ConstructorInitializationTestSmellDetector::class.java)

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PyElementVisitor() {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    val init = Arrays.stream(node.statementList.statements)
                        .filter { obj: PyStatement? -> PyFunction::class.java.isInstance(obj) }
                        .map { obj: PyStatement? ->
                            PyFunction::class.java.cast(obj)
                        }.anyMatch { pyFunction: PyFunction ->
                            pyFunction.name == "__init__"
                        }
                    if (init) {
                        holder.registerProblem(
                            node,
                            "Test smell: Constructor Initialization Test in class `${node.name}`",
                            ProblemHighlightType.WARNING
                        )
                    }
                }
            }
        }
    }

}