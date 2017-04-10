package bmazzarol.atom.plsql


import java.io.{ByteArrayInputStream, InputStream}

import akka.actor.ActorSystem
import colossus._
import colossus.core._
import colossus.protocols.http.HttpMethod._
import colossus.protocols.http.UrlParsing._
import colossus.protocols.http._
import colossus.service.Callback
import colossus.service.Callback.Implicits._
import com.trivadis.oracle.plsql.validation.{PLSQLJavaValidator, PLSQLValidatorPreferences}
import com.trivadis.oracle.sqlplus.SQLPLUSStandaloneSetup
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.validation.CheckMode

import scala.collection.JavaConversions._
import scala.util.Try


/**
  * PLSQL Lint Server.
  */
object PLSQLLintServer extends App {

  override def main(args: Array[String]): Unit = {

    // actor system and io system
    implicit val sys = ActorSystem()
    implicit val system = IOSystem()

    // shared headers
    val headers = HttpHeaders(HttpHeader(HttpHeaders.ContentType, ContentType.ApplicationJson))

    // shared body decoder
    implicit val decoder = new HttpBodyDecoder[InputStream] {
      override def decode(body: Array[Byte]): Try[InputStream] = Try {
        new ByteArrayInputStream(body)
      }
    }

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
    val resource = resourceSet.createResource(URI.createURI("sharedResource.sql"))

    /**
      * Location of the server.
      *
      * @param root root path
      * @param port port
      */
    case class ServerLocation(root: String, port: Int)

    /**
      * Lints a given file and returns any issues as a json array.
      *
      * @param path path to the file on the editor
      * @param data file data
      * @return linter results
      */
    def lintFile(path: String, data: InputStream): String = {
      resource.unload()
      resource.load(data, null)
      s"[${
        validator.validate(resource, CheckMode.ALL, null).map(i => {
          s"""{"severity":"${i.getSeverity.name().toLowerCase}","location":{"file":"$path","position":[${i.getLineNumber},${i.getColumn}]},"excerpt":"${i.getMessage}"}"""
        }).mkString(",")
      }]"
    }

    // create a new server on provided root and path
    Server.basic("plsql-lint-server", args.head.toInt) {
      new HttpService(_) {
        def handle: PartialFunction[HttpRequest, Callback[HttpResponse]] = {
          case req@Post on Root / path => req.ok(lintFile(path, req.body.as[InputStream].get), headers)
        }
      }
    }
  }
}
