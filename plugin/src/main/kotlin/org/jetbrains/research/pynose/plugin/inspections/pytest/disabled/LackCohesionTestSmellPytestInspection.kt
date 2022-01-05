package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.LackCohesionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class LackCohesionTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(LackCohesionTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PyInspectionVisitor {
        return object : LackCohesionTestSmellVisitor(holder, session) {

            override fun visitPyFile(file: PyFile) {
                super.visitPyFile(file)
                if (PytestInspectionsUtils.isValidPytestFile(file)) {
                    val cosineSimilarityScores: MutableMap<Pair<PyFunction, PyFunction>, Double> = mutableMapOf()
                    testClassCohesionScore = 0.0
                    val classes = PytestInspectionsUtils.gatherPytestClasses(file)
                    if (classes.isEmpty()) {
                        val methodList = PytestInspectionsUtils.gatherValidPytestMethods(file)
                        processMethodList(methodList, cosineSimilarityScores)
                        if (1 - testClassCohesionScore >= threshold && cosineSimilarityScores.isNotEmpty()) {
                            registerLackCohesion(file) // todo: what to register?
                        }
                    } else {
                        for (c in classes) {
                            val methodList = c.statementList.statements
                                .filterIsInstance<PyFunction>()
                                .filter { pyFunction -> PytestInspectionsUtils.isValidPytestMethodInsideFile(pyFunction) }
                            processMethodList(methodList, cosineSimilarityScores)
                            if (1 - testClassCohesionScore >= threshold && cosineSimilarityScores.isNotEmpty()) {
                                registerLackCohesion(c.nameIdentifier!!)
                            }
                        }
                    }
                }
            }
        }
    }
}