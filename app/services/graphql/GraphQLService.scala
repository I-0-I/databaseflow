package services.graphql

import models.graphql.{GraphQLContext, GraphQLSchema}
import models.user.User
import play.api.libs.json.{JsObject, Json}
import sangria.execution.{Executor, HandledException}
import sangria.parser.QueryParser
import sangria.marshalling.playJson._
import utils.ApplicationContext
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.util.{Failure, Success}

object GraphQLService {
  private[this] val exceptionHandler: Executor.ExceptionHandler = {
    case (_, e: IllegalStateException) => HandledException(e.getMessage)
  }

  def executeQuery(app: ApplicationContext, query: String, variables: Option[JsObject], operation: Option[String], user: User) = {
    val ctx = GraphQLContext(app, user)
    QueryParser.parse(query) match {
      case Success(ast) => Executor.execute(
        schema = GraphQLSchema.schema,
        queryAst = ast,
        userContext = ctx,
        operationName = operation,
        variables = variables.getOrElse(Json.obj()),
        //deferredResolver = GraphQLResolver,
        exceptionHandler = exceptionHandler,
        maxQueryDepth = Some(10)
      )
      case Failure(error) => throw error
    }
  }

  def parseVariables(variables: String) = if (variables.trim == "" || variables.trim == "null") {
    Json.obj()
  } else {
    Json.parse(variables).as[JsObject]
  }
}
