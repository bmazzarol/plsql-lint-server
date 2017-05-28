package bmazzarol.plsql

import java.io.{ByteArrayInputStream, InputStream}

import colossus.protocols.http.{ContentType, HttpBody, HttpBodyDecoder, HttpBodyEncoder, HttpHeader, HttpHeaders}
import io.circe.Json

import scala.util._

/**
  * Colossus encoders.
  */
object HTTPEncoders {

  implicit val stringDecoder = new HttpBodyDecoder[String] {
    override def decode(body: Array[Byte]): Try[String] = Try {
      new String(body, "UTF_8")
    }
  }

  implicit val jsonEncoder = new HttpBodyEncoder[Json] {
    override def encode(data: Json): HttpBody = new HttpBody(data.noSpaces.getBytes, Some(HttpHeader(HttpHeaders.ContentType, ContentType.ApplicationJson)))
  }
}
