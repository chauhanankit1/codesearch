package com.codesearch.handler

import akka.actor.{Actor, Props}
import com.codesearch.service.SearchEngine
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by ankchauh on 9/19/2017.
  */

object RequestHandler {
  def props(): Props = {
    Props(classOf[RequestHandler])
  }
}

case class SearchRequest(query: String)

case class SearchResponse(results: String)

//Actor to handle incoming search requests
class RequestHandler extends Actor with LazyLogging {

  def receive: Receive = {
    case request: SearchRequest => {
      logger.info(s" Received SearchRequest for keyword {${request.query}}")
      if (request.query.length > 0) {
        //extract the query string and pass it to the search engine
        val result = new SearchEngine().search(request.query)
        //respond back with search results
        sender() ! SearchResponse(result)
      } else {
        sender() ! SearchResponse("Invalid search keyword ... some special chars like (#,$,+..) aren't supported in query")
      }
    }
    case _ =>
  }
}
