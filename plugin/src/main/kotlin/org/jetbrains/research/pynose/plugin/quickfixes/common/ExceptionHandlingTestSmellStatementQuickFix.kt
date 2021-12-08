package org.jetbrains.research.pynose.plugin.quickfixes.common

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.jetbrains.python.codeInsight.imports.AddImportHelper
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyTryExceptStatement
import com.jetbrains.python.psi.impl.PyWithStatementImpl
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle

class ExceptionHandlingTestSmellStatementQuickFix(
    private val currentFile: PsiFile,
    private val isUnittestMode: Boolean
) :
    LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.exception.try_except.message")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement
        val elementGenerator = PyElementGenerator.getInstance(project)
        when (element) {
            is PyTryExceptStatement -> {
                val tryPartCh = element.tryPart.children
                val tryPart = tryPartCh[0]
                val exceptPart = element.exceptParts
                val errorType = exceptPart[0].children[0].text
                val newExpressionText = if (isUnittestMode) {
                    "with self.assertRaises(" + errorType + "):\n    " + tryPart.text
                } else {
                    "with pytest.raises(" + errorType + "):\n    " + tryPart.text
                }

                element.replace(
                    elementGenerator.createFromText(
                        LanguageLevel.forElement(element),
                        PyWithStatementImpl::class.java, newExpressionText
                    )
                )
                if (!isUnittestMode) {
                    AddImportHelper.addImportStatement(
                        currentFile as PyFile,
                        "pytest",
                        null,
                        AddImportHelper.ImportPriority.BUILTIN,
                        null
                    )
                }
            }
        }
    }
}