package com.codesearch.service

import java.io.File
import java.nio.charset.StandardCharsets._
import java.nio.file.{Files, Paths}

import com.codesearch.config.AppConfig
import com.codesearch.model.{WordIndex, WordMap}
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jgit.api.Git

/**
  * Created by ankchauh on 9/19/2017.
  *
  * This class contains methods for building the search index
  */
object SearchIndex extends LazyLogging {

  def buildIndex(): Unit = {
    val dir: File = new File(new File("").getAbsolutePath + File.separator + AppConfig.codebaseSink)
    if (dir.exists()) {
      //TODO: If directory already exists, resync the latest changes from git repo
    } else {
      logger.info("Cloning the git repo .... please wait ...")
      val git = Git.cloneRepository()
        .setURI(AppConfig.gitRepo)
        .setDirectory(dir)
        .call()
      logger.info("...Done...")
    }

    //Get list of all files and add their content to search index
    val files = getListOfFilesRecursive(dir)
    files.foreach(x => {
      addWordsToIndex(x)
    })
  }

  private def getListOfFilesRecursive(dir: File): Array[File] = {
    val curr = dir.listFiles()
    //If current directory name startsWith '.' (like .git) then ignore it for indexation
    curr ++ curr.filter(x => x.isDirectory && !x.getName.startsWith(".")).flatMap(getListOfFilesRecursive)
  }

  private def addWordsToIndex(file: File): Unit = {
    try {
      //Indexing words with min 2 chars
      val minWordSize = 2
      if (file.isFile) {
        val content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath)), UTF_8)
        var i = 1
        //First split file content on lines, then on words and add word to search index with File name and line number
        content.split("\\r?\\n").foreach(x => {
          x.trim.split("\\s+").filter(_.size >= minWordSize).foreach(y => WordMap.put(y, WordIndex(i, file)))
          i = i + 1
        })
      }
    } catch {
      /*
       * TODO: Currently generic exception handling done. Could be extended to throw/log custom exceptions encountered
       *       while file parsing for detailed analysis
       */
      case e: Exception => logger.error(e.getStackTraceString)
    }

  }
}
