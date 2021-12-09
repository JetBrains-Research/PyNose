package org.jetbrains.research.pynose.plugin.quickfixes.unittest

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.PyNames
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PyUtil.sure
import com.jetbrains.python.psi.impl.PyFunctionBuilder
import com.jetbrains.python.psi.impl.PyFunctionImpl
import com.jetbrains.python.psi.types.PyClassTypeImpl
import com.jetbrains.python.refactoring.PyPsiRefactoringUtil
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle


class ConstructorInitializationTestSmellQuickFix : LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.constructor.message")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        // I'm sincerely sorry for everything written below
        val problemElement = descriptor.psiElement
        val body = problemElement.parent.text
            .replace("^def __init__\\(.*?\\):\n".toRegex(), "")
            .replace("super\\(.*?.\\)\\.__init__\\(.*?\\)\n".toRegex(), "")
        val newExpressionText = "def setUp(self):\n$body"
        val cls =
            PyClassTypeImpl(PsiTreeUtil.getParentOfType(problemElement, PyClass::class.java)!!, false).pyClass
        val clsStatements = cls.statementList
        sure(FileModificationService.getInstance().preparePsiElementForWrite(clsStatements))
        val builder = PyFunctionBuilder("setUp", cls)
        val method = builder.buildFunction()
        val setUpFunction =
            PyPsiRefactoringUtil.addElementToStatementList(method, clsStatements, PyNames.INIT == method.name)
        val elementGenerator: PyElementGenerator = PyElementGenerator.getInstance(project)
        setUpFunction.replace(
            elementGenerator.createFromText(
                LanguageLevel.forElement(setUpFunction),
                PyFunctionImpl::class.java,
                newExpressionText
            )
        )
        problemElement.parent.delete()
    }
}