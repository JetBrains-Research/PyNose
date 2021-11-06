package org.jetbrains.research.pynose.plugin.util

import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.psi.PyClass

abstract class AbstractTestSmellInspection : PyInspection() {

    protected var testCase: PyClass? = null

    abstract fun analyze()

    abstract fun reset()

    abstract fun reset(testCase: PyClass?)

    open fun getSmellName(): String? {
        val className = javaClass.name
        return className.substring("pynose.".length, className.length - "TestSmellInspection".length)
    }

    abstract fun hasSmell(): Boolean

    open fun getSmellDetail(): String? {
        return getSmellDetailJSON().toString()
    }

    abstract fun getSmellDetailJSON(): JsonObject

    protected open fun templateSmellDetailJSON(): JsonObject? {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", getSmellName())
        jsonObject.addProperty("hasSmell", hasSmell())
        return jsonObject
    }

    companion object {
        private val LOG = Logger.getInstance(AbstractTestSmellInspection::class.java)
    }
}