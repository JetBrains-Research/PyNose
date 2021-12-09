package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.LackCohesionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils
import org.jetbrains.research.pynose.plugin.util.UnittestInspectionsUtils

class LackCohesionTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(LackCohesionTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : LackCohesionTestSmellVisitor(holder, session) {

            override fun visitPyFile(file: PyFile) {
                super.visitPyFile(file)
                if (PytestInspectionsUtils.isValidPytestFile(file)) {
                    val cls = 
                }
            }

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