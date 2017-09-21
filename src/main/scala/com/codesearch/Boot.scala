package com.codesearch

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.codesearch.config.AppConfig
import com.codesearch.handler.{RequestHandler, SearchRequest, SearchResponse}
import com.codesearch.model.WordMap
import com.codesearch.service.SearchIndex
import com.typesafe.scalalogging.LazyLogging

import scala.io.StdIn

/**
  * Created by ankchauh on 9/17/2017.
  *
  * Main class of the application.
  * It implements a simple REST API for exposing the code search functionality over API
  * It also initializes an actor system for handling search requests.
  */
object Boot extends App with LazyLogging {

  implicit val system = ActorSystem("code-search-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val host = AppConfig.host
  private val port = AppConfig.port

  //Creating a request handler to handle search requests
  val requestHandler = system.actorOf(RequestHandler.props(), "requestHandler")

  //Route to execute search requests
  val route: Route = {

    implicit val timeout = Timeout(5, TimeUnit.SECONDS)

    /*
     * TODO: currently only GET requests are supported, could be extended to support POST for getting additional inputs like
     * allowing the user to specify whether the word is part of a function name, parameter list or variable name
     */

    //Search API is exposed over host:port/servicePath and expects a mandatory 'query' param
    val servicePath = "search"

    //Health check path for search api is at /health.
    val healthCheckPath = "health"

    path(servicePath) {
      get {
        parameters('query) { (query) =>
          onSuccess(requestHandler ? SearchRequest(query)) {
            case response: SearchResponse =>
              complete(StatusCodes.OK, s"${response.results}")
            case _ =>
              complete(StatusCodes.InternalServerError, "")
          }
        }
      }
    } ~
      path(healthCheckPath) {
        get {
          val indexSize = WordMap.getSize()
          indexSize match {
            case _ if (indexSize > 0) => complete(StatusCodes.OK, "Code Search Service is up and running !")
            case _ if (indexSize < 1) => complete(StatusCodes.InternalServerError, "Code Search Service is not running !")
          }
        }
      }
  }

  //Build the search index
  logger.info(s"Building search index ....")
  SearchIndex.buildIndex()
  logger.info(s"....Done....")

  //Startup, and listen for requests
  logger.info(s"Starting codesearch service ....")
  val bindingFuture = Http().bindAndHandle(route, host, port)
  logger.info(s"....Done....")

  logger.info(s"Waiting for requests at http://$host:$port/...\n Hit RETURN to terminate anytime! \n")
  StdIn.readLine()

  //Shutdown
  bindingFuture.flatMap(_.unbind())
  system.terminate()
}

