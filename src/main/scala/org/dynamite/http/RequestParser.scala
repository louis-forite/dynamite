package org.dynamite.http

import com.ning.http.client.Response
import org.dynamite.dsl.{AwsErrorSerializer, DynamoCommonError, JsonDeserialisationError}
import org.json4s._
import org.json4s.jackson.JsonMethods.{parse => jparse}

import scalaz.\/

private[dynamite] object RequestParser {
  implicit private val formats = DefaultFormats + new AwsErrorSerializer

  def parse(jsonString: String): DynamoCommonError \/ JValue =
    \/.fromTryCatchThrowable[JValue, Throwable](jparse(jsonString)) leftMap {
      _: Throwable =>
        //todo: add proper debug log here
        JsonDeserialisationError(s"The json $jsonString is not a valid string and was impossible to parse.")
    }

  def parseTo[A](jsonString: String)(implicit mf: scala.reflect.Manifest[A]): DynamoCommonError \/ A =
    \/.fromTryCatchThrowable[A, Throwable](jparse(jsonString).extract[A]) leftMap {
      t: Throwable =>
        //todo: add proper debug log here
        println(t)
        JsonDeserialisationError(s"An error occurred while trying to deserialise $jsonString")
    }
}

private[dynamite] object RequestExtractor {
  def extract(resp: Response): DynamoCommonError \/ String =
    \/.fromTryCatchThrowable[String, Throwable](resp.getResponseBody) leftMap {
      _: Throwable =>
        //todo: add proper debug log here
        JsonDeserialisationError("An error occurred while trying to read response from DynamoDB")
    }
}