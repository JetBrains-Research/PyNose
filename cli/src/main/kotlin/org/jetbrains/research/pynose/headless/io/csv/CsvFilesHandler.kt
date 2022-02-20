package org.jetbrains.research.pynose.headless.io.csv

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiFile
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.bufferedWriter

class CsvFilesHandler {

    private val separator = File.separatorChar

    fun writeToAggregatedCsv(outputDir: String, testFramework: String, agCsvData: MutableList<MutableList<String>>) {
        val agCsvOutputFileName = "$outputDir${separator}$testFramework${separator}aggregated_stats.csv"
        val csvFile = File(agCsvOutputFileName)
        File("$outputDir${separator}pytest").mkdirs()
        csvFile.createNewFile()
        val writer = Paths.get(agCsvOutputFileName).bufferedWriter()
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
        agCsvData.forEach { csvPrinter.printRecord(it) }
        csvPrinter.flush()
        csvPrinter.close()
    }

    fun gatherCsvInformation(
        name: String,
        holder: ProblemsHolder,
        psiFile: PsiFile,
        csvMap: MutableMap<String, MutableSet<PsiFile>>,
    ) {
        csvMap.getOrPut(name) { mutableSetOf() }
        if (holder.resultCount > 0) {
            csvMap[name]!!.add(psiFile)
        }
    }

    fun writeToCsvFile(
        outputDir: String, projectName: String, csvMap: MutableMap<String, MutableSet<PsiFile>>,
        fileCount: Int, hasHeader: Boolean, aggregatedData: MutableList<MutableList<String>>, frameworkName: String
    ) {
        val sortedCsvMap = TreeMap(csvMap)
        val csvOutputFileName =
            "$outputDir${separator}$frameworkName${projectName}_stats.csv"
        File("$outputDir${separator}$frameworkName").mkdirs()
        File(csvOutputFileName).createNewFile()
        val writer = Paths.get(csvOutputFileName).bufferedWriter()
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT)
        val header = mutableListOf("project_name", "test_file_count")
        val data = mutableListOf(projectName, fileCount.toString())

        sortedCsvMap.keys.forEach { header.add(it) }
        sortedCsvMap.keys.forEach { data.add(sortedCsvMap[it]?.size.toString()) }
        if (!hasHeader) {
            aggregatedData.add(header)
        }
        aggregatedData.add(data)
        csvPrinter.use {
            it.printRecord(header)
            it.printRecord(data)
        }
    }
}