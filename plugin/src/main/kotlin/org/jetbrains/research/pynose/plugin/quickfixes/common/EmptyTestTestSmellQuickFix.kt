package org.jetbrains.research.pynose.plugin.quickfixes.common

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageInfo
import com.intellij.usages.UsageInfoToUsageConverter
import com.intellij.usages.UsageTarget
import com.intellij.usages.UsageViewManager
import com.intellij.usages.UsageViewPresentation
import com.intellij.util.ArrayUtil
import com.intellij.util.CommonProcessors
import com.jetbrains.python.findUsages.PyFindUsagesHandlerFactory
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.annotations.NotNull
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle


class EmptyTestTestSmellQuickFix : LocalQuickFix {
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.empty.message")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val currentMethod = descriptor.psiElement.parent as PyFunction
        val usageInfos = findUsages(currentMethod)
        if (usageInfos.isEmpty()) {
            currentMethod.delete()
        } else {
            ApplicationManager.getApplication().invokeLater {
                val presentation = UsageViewPresentation()
                presentation.tabText = TestSmellBundle.message("usages.tool.window.content.name")
                presentation.codeUsagesString = TestSmellBundle.message("usages.tool.window.code.usages.string")
                val usages = UsageInfoToUsageConverter.convert(PsiElement.EMPTY_ARRAY, usageInfos.toTypedArray())
                UsageViewManager.getInstance(project).showUsages(UsageTarget.EMPTY_ARRAY, usages, presentation)
            }
        }
    }

    private fun findUsages(@NotNull element: PsiElement): Collection<UsageInfo?> {
        val handler = PyFindUsagesHandlerFactory().createFindUsagesHandler(element, false)
            ?: return emptyList()
        val processor: CommonProcessors.CollectProcessor<UsageInfo> = CommonProcessors.CollectProcessor()
        val psiElements: Array<PsiElement> = ArrayUtil.mergeArrays(handler.primaryElements, handler.secondaryElements)
        val options: FindUsagesOptions = handler.getFindUsagesOptions(null)
        for (psiElement in psiElements) {
            handler.processElementUsages(psiElement, processor, options)
        }
        return processor.results
    }
}
