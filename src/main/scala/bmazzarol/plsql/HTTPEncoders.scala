package bmazzarol.plsql

import java.io.{ByteArrayInputStream, InputStream}

import colossus.protocols.http.{ContentType, HttpBody, HttpBodyDecoder, HttpBodyEncoder, HttpHeader, HttpHeaders}
import io.circe.Json

import scala.util.Try

/**
  * Colossus encoders.
  */
object HTTPEncoders {

  implicit val decoder = new HttpBodyDecoder[InputStream] {
    override def decode(body: Array[Byte]): Try[InputStream] = Try {
      new ByteArrayInputStream(body)
    }
  }

  implicit val jsonEncoder = new HttpBodyEncoder[Json] {
    override def encode(data: Json): HttpBody = new HttpBody(data.noSpaces.getBytes, Some(HttpHeader(HttpHeaders.ContentType, ContentType.ApplicationJson)))
  }
}
