package bmazzarol.plsql

import java.io.ByteArrayInputStream

import akka.actor.ActorSystem
import bmazzarol.plsql.AST.{Filters, LintFileRequest}
import bmazzarol.plsql.HTTPEncoders._
import colossus._
import colossus.core._
import colossus.protocols.http.HttpMethod._
import colossus.protocols.http.UrlParsing._
import colossus.protocols.http._
import colossus.service.Callback
import colossus.service.Callback.Implicits._
import com.trivadis.oracle.plsql.validation.{PLSQLJavaValidator, PLSQLValidatorPreferences}
import com.trivadis.oracle.sqlplus.SQLPLUSStandaloneSetup
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry
import org.eclipse.xtext.resource.{XtextResource, XtextResourceSet}
import org.eclipse.xtext.validation.{CheckMode, Issue}

import scala.collection.JavaConversions._

/**
  * PLSQL Lint Server.
  */
object PLSQLLintServer extends App {

  override def main(args: Array[String]): Unit = {

    // actor system and io system
    implicit val sys = ActorSystem()
    implicit val system = IOSystem()

    /**
      * Core linting classes.
      */
    val validatorClass: Class[PLSQLJavaValidator] = Class.forName("com.trivadis.tvdcc.validators.TrivadisGuidelines3").asInstanceOf[Class[PLSQLJavaValidator]]
    val setup = {
      PLSQLValidatorPreferences.INSTANCE.setValidatorClass(validatorClass)
      new SQLPLUSStandaloneSetup
    }
    val plusInjector = setup.createInjectorAndDoEMFRegistration
    val provider = Registry.INSTANCE.getResourceServiceProvider(URI.createURI("dummy:/example.sql"))
    val validator = provider.getResourceValidator
    val resourceSet = plusInjector.getInstance(classOf[XtextResourceSet])
    resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, true)

    /**
      * Returns true if the issue should b returned
      *
      * @param path          path to the file
      * @param issue         issue to filter on
      * @param globalFilters global filters to always apply
      * @param filters       filters to apply
      * @return true if the issue should be returned
      */
    def returnIssue(path: String, issue: Issue, globalFilters: Option[Filters], filters: Seq[Filters]): Boolean = (globalFilters, filters.find(_.path.contains(path))) match {
      case (Some(Filters(_, gCodes, _)), Some(Filters(Some(_), codes, Some(ig)))) => !(if (ig) gCodes ++ codes else codes).contains(issue.getCode)
      case (None, Some(Filters(Some(_), codes, _)))                               => !codes.contains(issue.getCode)
      case (Some(Filters(_, gCodes, _)), None)                                    => !gCodes.contains(issue.getCode)
      case _                                                                      => true
    }

    /**
      * Lints a file.
      *
      * @param req lint-file request
      * @return lint-file response
      */
    def lintFile(req: HttpRequest): HttpResponse = req.body.as[String].map(decode[LintFileRequest](_) match {
      case Left(error) => req.error(s"Failed to lint file. $error".asJson)
      case Right(lr)   =>
        val uri = URI.createFileURI(lr.path)
        val resource = if (resourceSet.getURIResourceMap.containsKey(uri)) resourceSet.getResource(uri, false) else resourceSet.createResource(uri)
        resource.unload()
        resource.load(new ByteArrayInputStream(lr.content.getBytes), null)
        req.ok(validator.validate(resource, CheckMode.ALL, null)
          .filter(returnIssue(lr.path, _, lr.filters.find(_.isGlobal), lr.filters.filter(!_.isGlobal)))
          .flatMap(AST.message(lr.path, _))
          .asJson)
    }).getOrElse(req.error("Failed to parse JSON request.".asJson))

    // create a new server on provided root and path
    Server.basic("plsql-lint-server", args.head.toInt) {
      new HttpService(_) {
        def handle: PartialFunction[HttpRequest, Callback[HttpResponse]] = {
          case req@Get on Root / "version"     => req.ok("1.0.2".asJson)
          case req@Get on Root / "check-alive" => req.ok("ok".asJson)
          case req@Get on Root / "shutdown"    => system.apocalypse(); req.ok("shutting down...".asJson)
          case req@Post on Root / "lint-file"  => lintFile(req)
        }
      }
    }
  }
}
