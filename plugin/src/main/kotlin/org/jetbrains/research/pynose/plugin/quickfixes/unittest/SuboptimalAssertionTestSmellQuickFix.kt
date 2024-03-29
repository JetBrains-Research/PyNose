package org.jetbrains.research.pynose.plugin.quickfixes.unittest

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.siblings
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.impl.PyExpressionStatementImpl
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle


class SuboptimalAssertionTestSmellQuickFix : LocalQuickFix {

    private lateinit var elementGenerator: PyElementGenerator

    // todo: specify replacement name for each assertion?
    override fun getFamilyName(): String {
        return TestSmellBundle.message("quickfixes.suboptimal.message")
    }

    private fun processAssertionArgsType(assertCall: PyCallExpression) : PyBinaryExpression? {
        var binaryExpression = assertCall.arguments[0]
        if (binaryExpression is PyParenthesizedExpression) {
            binaryExpression = PsiTreeUtil.getChildOfType(binaryExpression, PyBinaryExpression::class.java)
        }
        if (binaryExpression !is PyBinaryExpression) {
            return null
        }
        return binaryExpression
    }

    private fun processAssertTrue(assertCall: PyCallExpression) {
        if (assertCall.arguments.isEmpty()) return
        val binaryExpression = processAssertionArgsType(assertCall)?: return
        val children = binaryExpression.children
        val op = binaryExpression.psiOperator
        val assertionType = when (op?.text) {
            "==" -> "assertEqual"
            "!=" -> "assertNotEqual"
            "in" -> "assertIn"
            "<" -> "assertLess"
            "<=" -> "assertLessEqual"
            ">" -> "assertGreater"
            ">=" -> "assertGreaterEqual"
            "is" -> {
                if (op.siblings().any { s -> s.text == "not" }) {
                    "assertIsNot"
                } else {
                    "assertIs"
                }
            }
            "not" -> {
                if (op.siblings().any { s -> s.text == "in" }) {
                    "assertNotIn"
                } else {
                    null
                }
            }
            else -> null
        }
        replaceTwoArgumentsWithOptimal(assertCall, assertionType, children)
    }

    private fun processAssertFalse(assertCall: PyCallExpression) {
        if (assertCall.arguments.isEmpty()) return
        val binaryExpression = processAssertionArgsType(assertCall)?:return
        val children = binaryExpression.children
        val op = binaryExpression.psiOperator
        val assertionType = when (op?.text) {
            "==" -> "assertNotEqual"
            "!=" -> "assertEqual"
            "in" -> "assertNotIn"
            "<" -> "assertGreaterEqual"
            "<=" -> "assertGreater"
            ">" -> "assertLessEqual"
            ">=" -> "assertLess"
            "is" -> {
                if (op.siblings().any { s -> s.text == "not" }) {
                    "assertIs"
                } else {
                    "assertIsNot"
                }
            }
            "not" -> {
                if (op.siblings().any { s -> s.text == "in" }) {
                    "assertIn"
                } else {
                    null
                }
            }
            else -> null
        }
        replaceTwoArgumentsWithOptimal(assertCall, assertionType, children)
    }

    private fun processPositiveTwoParamAssert(assertCall: PyCallExpression) {
        val arg1 = assertCall.arguments[0] ?: return
        val arg2 = assertCall.arguments[1] ?: return
        val assertionType = when (arg2.text) {
            "False" -> "assertFalse"
            "True" -> "assertTrue"
            "None" -> "assertIsNone"
            else -> null
        }
        replaceOneArgumentWithOptimal(assertCall, assertionType, arg1)
    }

    private fun processNegativeTwoParamAssert(assertCall: PyCallExpression) {
        val arg1 = assertCall.arguments[0] ?: return
        val arg2 = assertCall.arguments[1] ?: return
        val assertionType = when (arg2.text) {
            "False" -> "assertTrue"
            "True" -> "assertFalse"
            "None" -> "assertIsNotNone"
            else -> null
        }
        replaceOneArgumentWithOptimal(assertCall, assertionType, arg1)
    }

    private fun replaceTwoArgumentsWithOptimal(
        assertCall: PyCallExpression,
        assertionType: String?,
        children: Array<PsiElement>,
    ) {
        if (children.size < 2) return
        val newExpressionText = "self.$assertionType(" + children[0].text + "," + children[1].text + ")"
        assertCall.parent.replace(
            elementGenerator.createFromText(
                LanguageLevel.forElement(assertCall),
                PyExpressionStatementImpl::class.java,
                newExpressionText
            )
        )
    }

    private fun replaceOneArgumentWithOptimal(
        assertCall: PyCallExpression,
        assertionType: String?,
        argument: PsiElement,
    ) {
        val newExpressionText = "self.$assertionType(" + argument.text + ")"
        assertCall.parent.replace(
            elementGenerator.createFromText(
                LanguageLevel.forElement(assertCall),
                PyExpressionStatementImpl::class.java,
                newExpressionText
            )
        )
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val assertCall = descriptor.psiElement as PyCallExpression
        val callee = assertCall.callee ?: return
        elementGenerator = PyElementGenerator.getInstance(project)
        when (callee.name) {
            "assertTrue" -> {
                processAssertTrue(assertCall)
            }
            "assertFalse" -> {
                processAssertFalse(assertCall)
            }
            "assertEqual" -> {
                processPositiveTwoParamAssert(assertCall)
            }
            "assertNotEqual" -> {
                processNegativeTwoParamAssert(assertCall)
            }
            "assertIs" -> {
                processPositiveTwoParamAssert(assertCall)
            }
            "assertIsNot" -> {
                processNegativeTwoParamAssert(assertCall)
            }
        }
    }
}
