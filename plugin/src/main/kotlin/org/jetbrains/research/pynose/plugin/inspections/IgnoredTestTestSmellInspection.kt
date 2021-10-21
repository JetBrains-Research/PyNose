package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.*
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.core.detectors.impl.ConstructorInitializationTestSmellDetector

open class IgnoredTestTestSmellInspection : PyInspection() {
    private val LOG = Logger.getInstance(ConstructorInitializationTestSmellDetector::class.java)
    private val testHasSkipDecorator: MutableMap<PyFunction, Boolean> = mutableMapOf()
    private val decoratorText = "@unittest.skip"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PyElementVisitor {

        fun registerIgnored(valueParam: PsiElement) {
            holder.registerProblem(
                valueParam,
                "Consider removing or modifying ignored tests to avoid overhead and improve code comprehension",
                ProblemHighlightType.WARNING
            )
        }

        return object : PyElementVisitor() {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    PyNoseUtils.gatherTestMethods(node).forEach { testMethod ->
                        visitPyElement(testMethod)
                    }
                    node.decoratorList?.decorators?.forEach { decorator ->
                        if (decorator.text.startsWith(decoratorText)) {
                            registerIgnored(node.nameIdentifier!!)
                        }
                    }
                    testHasSkipDecorator.filter { element -> element.value }.forEach { decorator ->
                        registerIgnored(decorator.key.nameIdentifier!!)
                    }
                }
                testHasSkipDecorator.clear()
            }

            override fun visitPyDecorator(decorator: PyDecorator) {
                if (!decorator.text.startsWith(decoratorText)) {
                    for (element in decorator.children) {
                        visitPyElement(element!! as PyElement)
                    }
                    return
                }
                if (decorator.target != null) {
                    testHasSkipDecorator.putIfAbsent(decorator.target!!, true)
                }
            }
        }
    }
}