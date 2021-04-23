package pynose;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import opennlp.tools.stemmer.PorterStemmer;

import java.util.*;
import java.util.stream.Collectors;

public class LackCohesionTestSmellDetector extends AbstractTestSmellDetector {
    private static final PorterStemmer STEMMER = new PorterStemmer();
    private static final Logger LOGGER = Logger.getInstance(LackCohesionTestSmellDetector.class);
    private static final Set<String> STOP_WORDS = new HashSet<>(
            Arrays.asList(
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
                    "with", "yield")
    );
    private final Map<Pair<PyFunction, PyFunction>, Double> cosineSimilarityScores;
    public boolean splitIdentifier = true;
    public boolean removeStopWords = false;
    public double threshold = 0.6;  // from the paper

    private double testClassCohesionScore;

    public LackCohesionTestSmellDetector(PyClass aTestCase) {
        testCase = aTestCase;
        cosineSimilarityScores = new HashMap<>();
    }

    @Override
    public void analyze() {
        var methodList = Util.gatherTestMethods(testCase);
        for (var i = 0; i < methodList.size(); ++i) {
            for (var j = i + 1; j < methodList.size(); ++j) {
                var score = calculateCosineSimilarityBetweenMethods(methodList.get(i), methodList.get(j));
                cosineSimilarityScores.put(new Pair<>(methodList.get(i), methodList.get(j)), score);
            }
        }

        testClassCohesionScore = cosineSimilarityScores.values().stream()
                .mapToDouble(d -> d)
                .average()
                .orElse(0.0);
    }

    @Override
    public void reset() {
        cosineSimilarityScores.clear();
    }

    @Override
    public void reset(PyClass aTestCase) {
        testCase = aTestCase;
        reset();
    }

    @Override
    public boolean hasSmell() {
        return (1 - testClassCohesionScore) >= threshold && cosineSimilarityScores.size() >= 1;
    }

    @Override
    public JsonObject getSmellDetailJSON() {
        var jsonObject = templateSmellDetailJSON();
        var detailObject = new JsonObject();
        detailObject.addProperty("testClassCohesionScore", testClassCohesionScore);
        detailObject.addProperty("threshold", threshold);
        var cosineSimilarityArray = new JsonArray();
        cosineSimilarityScores.forEach((ffPair, score) -> {
            JsonArray cosineSimilarityEntry = new JsonArray();
            cosineSimilarityEntry.add(ffPair.first.getName());
            cosineSimilarityEntry.add(ffPair.second.getName());
            cosineSimilarityEntry.add(score);
            cosineSimilarityArray.add(cosineSimilarityEntry);
        });
        detailObject.add("cosineSimilarityScores", cosineSimilarityArray);
        jsonObject.add("detail", detailObject);
        return jsonObject;
    }

    private List<String> extractMethodBody(PyFunction method) {
        var body = method.getText();
        var tokens = body.split(
                splitIdentifier ?
                        "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|\\s+|_|\\d+" :
                        "\\s+");
        return Arrays.stream(tokens)
                .map(String::toLowerCase)
                .filter(t -> !removeStopWords || !STOP_WORDS.contains(t))
                .map(STEMMER::stem)
                .collect(Collectors.toList());
    }

    private double calculateCosineSimilarityBetweenMethods(PyFunction m1, PyFunction m2) {
        List<String> tokens1 = extractMethodBody(m1), tokens2 = extractMethodBody(m2);
        Counter<String> vec1 = Counter.fromCollection(tokens1), vec2 = Counter.fromCollection(tokens2);

        var intersection = vec1.getItems().stream()
                .filter(vec2.getItems()::contains)
                .collect(Collectors.toSet());
        var numerator = intersection.stream()
                .mapToInt(commonToken -> vec1.getCount(commonToken) * vec2.getCount(commonToken))
                .sum();
        var sum1 = vec1.getItems().stream().mapToInt(t -> vec1.getCount(t) * vec1.getCount(t)).sum();
        var sum2 = vec2.getItems().stream().mapToInt(t -> vec2.getCount(t) * vec2.getCount(t)).sum();
        var denominator = Math.sqrt(sum1) * Math.sqrt(sum2);

        if (denominator == 0) {
            return 0.0;
        }

        return numerator / denominator;
    }
}

class Counter<T> {
    final Map<T, Integer> counts = new HashMap<>();

    public static <T> Counter<T> fromCollection(Collection<T> collection) {
        Counter<T> counter = new Counter<>();
        for (var item : collection) {
            counter.add(item);
        }
        return counter;
    }

    public void add(T t) {
        counts.merge(t, 1, Integer::sum);
    }

    public int getCount(T t) {
        return counts.getOrDefault(t, 0);
    }

    public Set<T> getItems() {
        return counts.keySet();
    }
}
