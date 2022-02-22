package org.jetbrains.research.pynose.headless.io.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyFunction
import org.apache.commons.lang3.tuple.MutablePair
import java.io.File
import java.io.IOException

class JsonFilesHandler {

    fun initOutputJsonFile(outputDir: String, projectName: String): File {
        val jsonOutputFileName = "$outputDir${File.separatorChar}${projectName}_ext_stats.json"
        val jsonFile = File(jsonOutputFileName)
        File(outputDir).mkdirs()
        jsonFile.createNewFile()
        return jsonFile
    }

    fun gatherJsonFunctionInformation(
        inspectionName: String,
        holder: ProblemsHolder,
        jsonFileResultArray: JsonArray
    ) {
        val jsonResult = JsonObject()
        jsonResult.addProperty("Test smell name", inspectionName)
        jsonResult.addProperty("Has smell", holder.resultsArray.isNotEmpty())
        val casesMap = mutableMapOf<String, MutablePair<Int, MutableList<String>>>()
        holder.resultsArray.forEach {
            val name = PsiTreeUtil.getParentOfType(it.psiElement, PyFunction::class.java)?.name
            casesMap.getOrPut(name!!) { MutablePair(0, mutableListOf()) }
            casesMap[name]!!.left += 1
            val range = it.textRangeInElement
            if (range != null) {
                casesMap[name]!!.right.add(it.psiElement.text.substring(range.startOffset, range.endOffset))
            } else {
                casesMap[name]!!.right.add(it.psiElement.text)
            }
        }
        val entry = JsonArray()
        casesMap.forEach { (ts, pair) ->
            entry.add(ts)
            entry.add(pair.left)
            val cases = JsonArray()
            pair.right.forEach { gathered -> cases.add(gathered) }
            entry.add(cases)
        }
        jsonResult.add("Detail", entry)
        jsonFileResultArray.add(jsonResult)
    }

    fun gatherJsonClassOrFileInformation(
        inspectionName: String,
        holder: ProblemsHolder,
        jsonFileResultArray: JsonArray
    ) {
        val jsonResult = JsonObject()
        jsonResult.addProperty("Test smell name", inspectionName)
        jsonResult.addProperty("Has smell", holder.resultsArray.isNotEmpty())
        val casesMap = mutableMapOf<String, Int>()
        holder.resultsArray.forEach { res ->
            var name = PsiTreeUtil.getParentOfType(res.psiElement, PyClass::class.java)?.name
            if (name == null) {
                name = PsiTreeUtil.getParentOfType(res.psiElement, PyFile::class.java)?.name
            }
            name?.let {
                casesMap.getOrPut(it) { 0 }
                casesMap[it] = casesMap[it]!!.plus(1)
            }
        }
        val entry = JsonArray()
        casesMap.forEach { (ts, n) ->
            entry.add(ts)
            entry.add(n)
        }
        jsonResult.add("Detail", entry)
        jsonFileResultArray.add(jsonResult)
    }

    fun writeToJsonFile(projectResult: JsonArray, jsonFile: File) {
        val jsonString =
            GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(projectResult.toString()))
        try {
            jsonFile.writeText(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}