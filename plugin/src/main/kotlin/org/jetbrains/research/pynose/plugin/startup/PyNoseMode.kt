package org.jetbrains.research.pynose.plugin.startup

object PyNoseMode {
    fun getPyNoseUnittestMode(): Boolean {
        return PyNoseStartupActivity.getPluginUnittestMode()
    }

    fun getPyNosePytestMode(): Boolean {
        return PyNoseStartupActivity.getPluginPytestMode()
    }
}