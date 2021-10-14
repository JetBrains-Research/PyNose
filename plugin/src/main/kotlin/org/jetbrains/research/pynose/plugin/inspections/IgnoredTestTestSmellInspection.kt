package org.jetbrains.research.pynose.plugin.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyDecorator
import com.jetbrains.python.psi.PyElementVisitor
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.core.PyNoseUtils
import org.jetbrains.research.pynose.core.detectors.impl.ConstructorInitializationTestSmellDetector
import org.jetbrains.research.pynose.plugin.util.AbstractTestSmellInspection

open class IgnoredTestTestSmellInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(ConstructorInitializationTestSmellDetector::class.java)
    private val testHasSkipDecorator: MutableMap<PyFunction, Boolean> = mutableMapOf()
    private val visitor: IgnoredTestVisitor = IgnoredTestVisitor()

    init {
        currentMethod = null
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PyElementVisitor() {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (PyNoseUtils.isValidUnittestCase(node)) {
                    for (testMethod in PyNoseUtils.gatherTestMethods(node)) {
                        currentMethod = testMethod
                        testHasSkipDecorator[currentMethod!!] = false
                        visitor.visitElement(currentMethod!!)
                    }
                    currentMethod = null
                    var classDecorator = false
                    val decoratorList = node.decoratorList
                    if (decoratorList != null) {
                        for (decorator in decoratorList.decorators) {
                            if (decorator.text.startsWith("@unittest.skip")) {
                                classDecorator = true
                            }
                        }
                    }
                    if (testHasSkipDecorator.containsValue(true) or classDecorator) {
                        holder.registerProblem(
                            node,
                            "Test smell: Ignored Test in class `${node.name}`",
                            ProblemHighlightType.WARNING
                        )
                    }
                }
                testHasSkipDecorator.clear()
            }
        }
    }

    inner class IgnoredTestVisitor : Companion.MyPsiElementVisitor() {
        fun visitPyDecorator(decorator: PyDecorator) {
            if (!decorator.text.startsWith("@unittest.skip")) {
                for (element in decorator.children) {
                    visitElement(element!!)
                }
                return
            }
            if (currentMethod == decorator.target) {
                testHasSkipDecorator.replace(currentMethod!!, true)
            }
        }
    }
}