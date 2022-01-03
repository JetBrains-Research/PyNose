package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyRecursiveElementVisitor
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.LackCohesionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class LackCohesionTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(LackCohesionTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyRecursiveElementVisitor {
        return object : LackCohesionTestSmellVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    val cosineSimilarityScores: MutableMap<Pair<PyFunction, PyFunction>, Double> = mutableMapOf()
                    testClassCohesionScore = 0.0
                    val methodList = UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                    processMethodList(methodList, cosineSimilarityScores)
                    if (1 - testClassCohesionScore >= threshold && cosineSimilarityScores.isNotEmpty()) {
                        registerLackCohesion(node.nameIdentifier!!)
                    }
                }
            }
        }
    }
}