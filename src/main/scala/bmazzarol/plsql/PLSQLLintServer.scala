package bmazzarol.plsql

import java.io.InputStream
import java.net.URLDecoder

import akka.actor.ActorSystem
import bmazzarol.plsql.AST.Message
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
import io.circe.syntax._
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry
import org.eclipse.xtext.resource.{XtextResource, XtextResourceSet}
import org.eclipse.xtext.validation.CheckMode

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
      * Lints a given file and returns any issues as a json array.
      *
      * @param data file data
      * @return linter results
      */
    def lintFile(path: String, data: InputStream): Seq[Message] = {
      val uri = URI.createURI(path)
      val resource = if (resourceSet.getURIResourceMap.containsKey(uri)) resourceSet.getResource(uri, false) else resourceSet.createResource(uri)
      resource.unload()
      resource.load(data, null)
      validator.validate(resource, CheckMode.ALL, null).flatMap(AST.message(path, _))
    }

    // create a new server on provided root and path
    Server.basic("plsql-lint-server", args.head.toInt) {
      new HttpService(_) {
        def handle: PartialFunction[HttpRequest, Callback[HttpResponse]] = {
          case req@Get on Root / "version"           => req.ok("1.0".asJson)
          case req@Get on Root / "check-alive"       => req.ok("ok".asJson)
          case req@Get on Root / "shutdown"          => system.apocalypse(); req.ok("shutting down...".asJson)
          case req@Post on Root / "lint-file" / path => req.ok(lintFile(URLDecoder.decode(path, "UTF-8"), req.body.as[InputStream].get).asJson)
        }
      }
    }
  }
}
