package org.jetbrains.research.pynose.plugin.startup

import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.Tools
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager

// source: https://github.com/briancabbott/programming-sandbox-kotlin

class ProfileTools {
    companion object {
        internal fun Project.enableSingleInspection(inspectionName: String) {
            InspectionProfileImpl.INIT_INSPECTIONS = true
            val profile = InspectionProfileImpl("$inspectionName-only")
            profile.disableAllTools(this)
            profile.enableTool(inspectionName, this)
            replaceProfile(profile)
        }

        private fun Project.replaceProfile(profile: InspectionProfileImpl) {
            preloadProfileTools(profile, this)
            val manager = InspectionProjectProfileManager.getInstance(this) as ProjectInspectionProfileManager
            manager.addProfile(profile)
            val prev = manager.currentProfile
            manager.setCurrentProfile(profile)
            Disposer.register(this) {
                InspectionProfileImpl.INIT_INSPECTIONS = false
                manager.setCurrentProfile(prev)
                manager.deleteProfile(profile)
            }
        }

        private fun preloadProfileTools(profile: InspectionProfileImpl, project: Project) {
            profile.getAllEnabledInspectionTools(project).forEach { state: Tools -> state.tool.getTool() }
        }
    }
}