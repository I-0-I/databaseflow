import sbt._

object Dependencies {
  val scapegoatVersion = "1.2.0"

  object Cache {
    val ehCache = "net.sf.ehcache" % "ehcache-core" % "2.6.11"
  }

  object Logging {
    val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.12"
  }

  object Jdbc {
    val postgres = "org.postgresql" % "postgresql" % "9.4.1208"
    val mysql = "mysql" % "mysql-connector-java" % "5.1.38"
  }

  object Play {
    private[this] val version = "2.4.6"
    val playFilters = play.sbt.PlayImport.filters
    val playWs = play.sbt.PlayImport.ws
    val playJson = play.sbt.PlayImport.json
    val playTest = "com.typesafe.play" %% "play-test" % version
  }

  object Akka {
    private[this] val version = "2.4.2"
    val actor = "com.typesafe.akka" %% "akka-actor" % version
    val remote = "com.typesafe.akka" %% "akka-remote" % version
    val logging = "com.typesafe.akka" %% "akka-slf4j" % version
    val cluster = "com.typesafe.akka" %% "akka-cluster" % version
    val clusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % version
    val clusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % version
    val testkit = "com.typesafe.akka" %% "akka-testkit" % version
  }

  object WebJars {
    val requireJs = "org.webjars" % "requirejs" % "2.1.22"
    val jquery = "org.webjars" % "jquery" % "2.2.1"
    val materialize = "org.webjars" % "materializecss" % "0.97.5"
  }

  object Mail {
    val mailer = "com.typesafe.play" %% "play-mailer" % "3.0.1"
  }

  object Metrics {
    val metrics = "nl.grons" %% "metrics-scala" % "3.5.2" withSources()
    val jvm = "io.dropwizard.metrics" % "metrics-jvm" % "3.1.2" withSources()
    val ehcache = "io.dropwizard.metrics" % "metrics-ehcache" % "3.1.2" withSources() intransitive()
    val healthChecks = "io.dropwizard.metrics" % "metrics-healthchecks" % "3.1.2" withSources() intransitive()

    val json = "io.dropwizard.metrics" % "metrics-json" % "3.1.2" withSources()

    val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % "9.3.7.v20160115" withSources()
    val servlets = "io.dropwizard.metrics" % "metrics-servlets" % "3.1.2" withSources() intransitive()
    val graphite = "io.dropwizard.metrics" % "metrics-graphite" % "3.1.2" withSources() intransitive()
  }
}
