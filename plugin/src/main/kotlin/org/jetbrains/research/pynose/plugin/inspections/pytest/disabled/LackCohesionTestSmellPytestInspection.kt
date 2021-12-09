package org.jetbrains.research.pynose.plugin.inspections.pytest.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.research.pynose.plugin.inspections.AbstractTestSmellInspection
import org.jetbrains.research.pynose.plugin.inspections.common.disabled.LackCohesionTestSmellVisitor
import org.jetbrains.research.pynose.plugin.util.PytestInspectionsUtils

class LackCohesionTestSmellPytestInspection : AbstractTestSmellInspection() {
    private val LOG = Logger.getInstance(LackCohesionTestSmellPytestInspection::class.java)

    override fun buildPytestVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : LackCohesionTestSmellVisitor(holder, session) {

            override fun visitPyFile(file: PyFile) {
                super.visitPyFile(file)
                if (PytestInspectionsUtils.isValidPytestFile(file)) {
                    val classes = PytestInspectionsUtils.gatherPytestClasses(file)
                    if (classes.isEmpty()) {
                        val methodList = PytestInspectionsUtils.gatherValidPytestMethods(file)
                        processMethodList(methodList)
                        if (1 - testClassCohesionScore >= threshold && cosineSimilarityScores.isNotEmpty()) {
                            registerLackCohesion(file) // todo: what to register?
                        }
                    } else {
                        for (c in classes) {
                            val methodList = c.statementList.statements
                                .filterIsInstance<PyFunction>()
                                .filter { pyFunction -> PytestInspectionsUtils.isValidPytestMethodInsideFile(pyFunction) }
                            processMethodList(methodList)
                            if (1 - testClassCohesionScore >= threshold && cosineSimilarityScores.isNotEmpty()) {
                                registerLackCohesion(c.nameIdentifier!!)
                            }
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
}