package org.jetbrains.research.pynose.plugin.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyElementVisitor
import org.jetbrains.research.pynose.core.PyNoseUtils
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.stream.Collectors

abstract class AbstractTestSmellInspection : PyInspection() {

    protected var testCase: PyClass? = null

    companion object {
        private val LOG = Logger.getInstance(AbstractTestSmellInspection::class.java)

        abstract class MyPyElementVisitor : PyElementVisitor() {
            override fun visitPyElement(element: PyElement) {
                val interfaces = Arrays.stream(element.javaClass.interfaces)
                    .filter { i: Class<*> ->
                        i.name.startsWith("com.jetbrains.python.psi")
                    }
                    .collect(Collectors.toList())
                if (interfaces.isEmpty()) {
                    LOG.warn(element.javaClass.name + " has no interface implemented")
                } else {

                    // Assumption: assuming all Python psi implementations have only one interface from PSI module
                    if (interfaces.size > 1) {
                        LOG.warn(
                            element.javaClass.name +
                                    " implements multiple interfaces from psi module: " +
                                    interfaces.stream().map { obj: Class<*> -> obj.name }
                                        .collect(Collectors.joining(","))
                        )
                    }
                    val anInterface = interfaces[0]
                    try {
                        val customVisitMethod = this.javaClass.getMethod(
                            "visit" + anInterface.simpleName,
                            anInterface
                        )
                        try {
                            customVisitMethod.invoke(this, element)
                            return
                        } catch (e: IllegalAccessException) {
                            LOG.warn(PyNoseUtils.exceptionToString(e))
                        } catch (e: InvocationTargetException) {
                            LOG.warn(PyNoseUtils.exceptionToString(e))
                        }
                    } catch (ignored: NoSuchMethodException) {
                    }
                }
                for (child: PsiElement in element.children) {
                    visitPyElement(child as PyElement)
                }
            }
        }
    }
}