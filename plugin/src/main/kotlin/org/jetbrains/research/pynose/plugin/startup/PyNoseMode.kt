package org.jetbrains.research.pynose.plugin.startup

import org.jetbrains.research.pynose.plugin.util.GeneralInspectionsUtils

object PyNoseMode {
    fun getPyNoseUnittestMode(): Boolean {
        return GeneralInspectionsUtils.getPluginUnittestMode()
    }

    fun getPyNosePytestMode(): Boolean {
        return GeneralInspectionsUtils.getPluginPytestMode()
    }
}