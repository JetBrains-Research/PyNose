package org.jetbrains.research.pynose.plugin.inspections.common.disabled

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyFunction
import opennlp.tools.stemmer.PorterStemmer
import org.jetbrains.research.pynose.plugin.util.TestSmellBundle
import java.util.*
import kotlin.math.sqrt

open class LackCohesionTestSmellVisitor(
    holder: ProblemsHolder?,
    session: LocalInspectionToolSession
) : PyInspectionVisitor(holder, session) {
    private val STEMMER = PorterStemmer()
    private val STOP_WORDS: Set<String> = setOf(
        "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself",
        "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself",
        "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that",
        "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
        "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as",
        "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through",
        "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off",
        "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how",
        "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not",
        "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should",
        "now", "False", "None", "True", "and", "as", "assert", "async", "await", "break", "class",
        "continue", "def", "del", "elif", "else", "except", "finally", "for", "from", "global", "if",
        "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", "while",
        "with", "yield"
    )
    protected val cosineSimilarityScores: MutableMap<Pair<PyFunction, PyFunction>, Double> = mutableMapOf()
    var splitIdentifier = true
    var removeStopWords = false
    protected var threshold = 0.6 // from the paper

    protected var testClassCohesionScore = 0.0

    fun registerLackCohesion(valueParam: PsiElement) {
        holder!!.registerProblem(
            valueParam,
            TestSmellBundle.message("inspections.lack.cohesion.description"),
            ProblemHighlightType.WARNING
        )
    }

    fun calculateCosineSimilarityBetweenMethods(m1: PyFunction, m2: PyFunction): Double {
        val tokens1: List<String> = extractMethodBody(m1)
        val tokens2: List<String> = extractMethodBody(m2)
        val vec1 = Counter.fromCollection(tokens1)
        val vec2 = Counter.fromCollection(tokens2)
        val intersection = vec1.getItems()
            .filter { o: String -> vec2.getItems().contains(o) }
            .toSet()
        val numerator = intersection.sumOf { commonToken: String ->
            vec1.getCount(commonToken) * vec2.getCount(commonToken)
        }
        val sum1 = vec1.getItems().sumOf { t: String ->
            vec1.getCount(t) * vec1.getCount(t)
        }
        val sum2 = vec2.getItems().sumOf { t: String ->
            vec2.getCount(t) * vec2.getCount(t)
        }
        val denominator = sqrt(sum1.toDouble()) * sqrt(sum2.toDouble())
        return if (denominator == 0.0) {
            0.0
        } else numerator / denominator
    }

    private fun extractMethodBody(method: PyFunction): List<String> {
        val body = method.text
        val tokens = body.split(
            if (splitIdentifier) "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|\\s+|_|\\d+" else "\\s+"
        ).toTypedArray()
        return tokens
            .map { obj: String -> obj.toLowerCase(Locale.getDefault()) }
            .filter { t: String? -> !removeStopWords || !STOP_WORDS.contains(t) }
            .map { s: String? -> STEMMER.stem(s) }
    }

    internal class Counter<T> {
        private val counts: MutableMap<T, Int> = mutableMapOf()

        fun add(t: T) {
            counts.merge(t, 1) { a: Int?, b: Int? -> Integer.sum(a!!, b!!) }
        }

        fun getCount(t: T): Int {
            return counts.getOrDefault(t, 0)
        }

        fun getItems(): Set<T> {
            return counts.keys
        }

        companion object {
            fun <T> fromCollection(collection: Collection<T>): Counter<T> {
                val counter: Counter<T> = Counter()
                collection.forEach { item -> counter.add(item) }
                return counter
            }
        }
    }
}