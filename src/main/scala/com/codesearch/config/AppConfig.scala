package com.codesearch.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Properties

/**
  * Created by ankchauh on 9/19/2017.
  */
object AppConfig {

  private val _config = ConfigFactory.load()

  //Rest API Configs
  lazy val host = getString(RestApiConstants.host)
  lazy val port = getInt(RestApiConstants.port)

  //Search Configs
  lazy val gitRepo = getString(ServiceConstants.gitRepoUri)
  lazy val codebaseSink = getString(ServiceConstants.localSink)

  private def getConfig: Config = _config

  private def getString(name: String): String = {
    Properties.envOrElse(
      name.toUpperCase.replaceAll("""\.""", "_"),
      getConfig.getString(name)
    )
  }

  private def getInt(name: String): Int = {
    Properties.envOrElse(
      name.toUpperCase.replaceAll("""\.""", "_"),
      getConfig.getString(name)
    ).toInt
  }
}

private object RestApiConstants {
  lazy val host = "app.interface"
  lazy val port = "app.port"
}

private object ServiceConstants {
  lazy val gitRepoUri = "git.repoUri"
  lazy val localSink = "local.sink"
}
