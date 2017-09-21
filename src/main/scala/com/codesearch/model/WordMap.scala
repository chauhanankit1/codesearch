package com.codesearch.model

import scala.collection.mutable.{ArrayBuffer, HashMap}

/**
  * Created by ankchauh on 9/16/2017.
  *
  * This is a custom implementation of a multimap to store word indices in the files in the following manner
  * "Word" -> {[lineNum,fileName],[lineNum,fileName]}
  */
object WordMap {
  private val map: HashMap[String, ArrayBuffer[WordIndex]] = new HashMap()

  def put(key: String, index: WordIndex) = {
    //If a word already exists in the search index, then add the current index to existing list of indices
    if (map.contains(key)) {
      val list = map.get(key).get
      list.prepend(index)
      map.put(key, list)
    } else {
      var list = new ArrayBuffer[WordIndex]
      list += index
      map.put(key, list)
    }
  }

  def get(key: String): Option[ArrayBuffer[WordIndex]] = {
    map.get(key)
  }

  def getSize(): Int = {
    map.size
  }

  def getMap(): HashMap[String, ArrayBuffer[WordIndex]] = {
    map
  }
}
