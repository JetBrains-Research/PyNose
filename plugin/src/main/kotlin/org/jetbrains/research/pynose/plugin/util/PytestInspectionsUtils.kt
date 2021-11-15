package org.jetbrains.research.pynose.plugin.util

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFunction

object PytestInspectionsUtils {
    fun isValidPytestParent(element: PsiElement): Boolean {
        return false // todo implement
    }

    fun isValidPytestMethod(testMethod: PyFunction): Boolean {
        return false // todo implement
    }
}