package com.codesearch.service

import java.io.File
import java.nio.charset.StandardCharsets._

import com.codesearch.config.AppConfig
import com.codesearch.model.{WordIndex, WordMap}
import org.apache.commons.io.FileUtils

import scala.collection.mutable.{ArrayBuffer, HashSet, StringBuilder}

/**
  * Created by ankchauh on 9/19/2017.
  *
  * This class contains methods for executing search and returning the relevant code snippets.
  *
  */
class SearchEngine {

  private val response = StringBuilder.newBuilder

  def search(query: String): String = {

    val start = System.currentTimeMillis()

    //Assumption: If the query is a phrase, split it into words, then search and display the collective results
    val keywords = query.split("\\s+")
    val allResults: ArrayBuffer[WordIndex] = new ArrayBuffer[WordIndex]()

    keywords.foreach(x => {
      allResults ++= executeSearch(x)
    })

    /*
    * Initial search results give a list of search hits(line numbers across all files)
    * Then merge the results to collect results as set of line numbers for a particular file
    */
    val mergedResults = mergeResults(allResults)

    //From the merged results, prepare the relevant code snippets
    prepareResults(mergedResults)

    val end = System.currentTimeMillis()

    val stats = new scala.StringBuilder(s"@@@@@@@@@@ Found ${allResults.size} results in ${mergedResults.size} "
      + s"files in ${end - start}ms while searching for { $query } @@@@@@@@\n")
    stats.append(response).append("\n\n############## End of Results ! ###########\n").toString()
  }

  //Method to search occurrences of given word in the search index
  private def executeSearch(word: String): ArrayBuffer[WordIndex] = {

    val searchResults: ArrayBuffer[WordIndex] = new ArrayBuffer()

    //first get all exact matches
    if (WordMap.getMap().contains(word)) {
      searchResults ++= WordMap.get(word).get
    }
    //Get the pattern matches
    WordMap.getMap().keySet.filter(x =>
      x.contains(word) || word.contains(x)).flatMap(WordMap.get).foreach(y =>
      searchResults ++= scala.util.Random.shuffle(y).take(2)) //randomly take 2 indexes for each pattern match

    searchResults
  }

  private def mergeResults(results: ArrayBuffer[WordIndex]): Map[File, List[Int]] = {
    val list = results.toList

    val listOfLines = list.groupBy(_.file).map(x => {
      val lineNumbers = x._2.map(y => y.line)
      (x._1, lineNumbers)
    })
    listOfLines
  }

  private def prepareResults(results: Map[File, List[Int]]) = {

    //Priority is given to those files which contains more occurrences of the searched word
    val prioritizedFiles = results.toSeq.sortBy(_._2.size).reverse

    prioritizedFiles.foreach(x => {

      val file = FileUtils.readLines(x._1, UTF_8.toString)

      //adding the filename to the search result
      response.append(s"\n########### File - ${
        x._1.getAbsolutePath.substring(x._1.getAbsolutePath
          .indexOf(AppConfig.codebaseSink))
      } ############\n")

      //sort the list of line numbers and get the window which contains the longest increasing subsequence
      val lineNumbers = x._2.sorted
      val window = getBestSnippet(lineNumbers)

      var startLine = lineNumbers(window._1)
      var endLine = lineNumbers(window._1 + window._2 - 1)

      val maxSnippetSize = 9
      val minSnippetSize = 4

      //preparing the relevant code snippets from search results
      if (endLine - startLine >= maxSnippetSize) {
        endLine = startLine + maxSnippetSize
      } else {
        var j = window._1 + window._2 - 1
        do {
          endLine = lineNumbers(j)
          j = j + 1
        } while (endLine - startLine < maxSnippetSize && j < lineNumbers.length)
        if (endLine - startLine >= maxSnippetSize) {
          endLine = startLine + maxSnippetSize
        }
      }

      if (endLine - startLine < minSnippetSize) {
        val filler = minSnippetSize - (endLine - startLine)
        startLine = Math.max((startLine - (filler / 2)), 1)
        endLine = Math.min((startLine + minSnippetSize), file.size())
      }

      //adding the selected snippet to response
      for (i <- startLine to endLine) {
        response.append(s"\n${i} " + file.get(i - 1))
      }
      response.append("\n#######################\n")
    })
  }

  /*
  * Method to accept a list of sorted integers and return the longest consecutive subsequence (LCS)
  * It actually returns a tuple which contains the start index and length of the LCS
  */
  private def getBestSnippet(x: List[Int]): (Int, Int) = {
    val hSet: HashSet[Int] = new HashSet()
    hSet ++= x

    var startIndex = 0
    var length = 0

    for (i <- 0 until x.length) {
      var currStart = 0
      var currLength = 0
      if (!hSet.contains(x(i) - 1)) {
        currStart = i
        var j = x(i)
        while (hSet.contains(j)) {
          currLength = currLength + 1
          j = j + 1
        }
        if (currLength > length) {
          startIndex = currStart
          length = currLength
        }
      }
    }

    (startIndex, length)
  }
}
