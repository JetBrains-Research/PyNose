package org.jetbrains.research.pynose.plugin.inspections.unittest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyClass
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.LackCohesionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class LackCohesionTestSmellUnittestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(LackCohesionTestSmellUnittestInspection::class.java)

    override fun buildUnittestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : LackCohesionTestSmellVisitor(holder, session) {
            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                if (UnittestInspectionsUtils.isValidUnittestCase(node)) {
                    val methodList = UnittestInspectionsUtils.gatherUnittestTestMethods(node)
                    for (i in methodList.indices) {
                        for (j in i + 1 until methodList.size) {
                            val score: Double = calculateCosineSimilarityBetweenMethods(methodList[i], methodList[j])
                            cosineSimilarityScores[Pair(methodList[i], methodList[j])] = score
                        }
                    }
                    testClassCohesionScore = cosineSimilarityScores
                        .values
                        .average()

                    if (1 - testClassCohesionScore >= threshold && cosineSimilarityScores.isNotEmpty()) {
                        registerLackCohesion(node.nameIdentifier!!)
                    }
                }
                testClassCohesionScore = 0.0
                splitIdentifier = true
                removeStopWords = false
                cosineSimilarityScores.clear()
            }
        }
    }
}