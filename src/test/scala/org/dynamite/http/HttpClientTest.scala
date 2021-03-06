package org.dynamite.http

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import org.dynamite.Arbitraries._
import org.dynamite.ValidJson
import org.dynamite.dsl._
import org.specs2.concurrent.FutureAwait
import org.specs2.{ScalaCheck, Specification}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scalaz.Scalaz._

class HttpClientTest
  extends Specification
    with FutureAwait
    with ScalaCheck
    with HttpServer { def is = s2"""
      Specifications for the HttpClient
        Firing a request and getting a valid response should yield the response body and the corresponding code $httpRequest
        Firing a request to an unreachable host should yield unreachable host error $unreachableHost
        Firing a request to an invalid host should yield invalid host error $invalidHost
        Firing a request and getting back an invalid response should yield an unexpected error $invalidResponse
  """

  def httpRequest = prop { (requestBody: ValidJson, responseBody: ValidJson, statusCode: StatusCode, headers: List[HttpHeader]) =>
    withHttpServer { httpServer =>
      val awsRequest = AwsHttpRequest(
        AwsHost(s"localhost:${httpServer.httpsPort()}/"),
        RequestBody(requestBody.json),
        headers)

      httpServer.stubFor {
        awsRequest.signedHeaders.foldLeft(post(urlEqualTo("/"))) { (b, header) =>
          b.withHeader(header.render._1, WireMock.equalTo(header.render._2))
        }.withRequestBody(equalToJson(requestBody.json))
          .willReturn(aResponse()
            .withStatus(statusCode.value)
            .withBody(responseBody.json))
      }

      Await.result(HttpClient.httpRequest(awsRequest).toEither, 10 seconds) fold(
        err => ko(s"The test is expected to succeed, got error $err instead"),
        succ => {
          succ.responseBody.value should be_==(responseBody)
          succ.statusCode.value should be_==(statusCode.value)
        })
    }
  }

  def unreachableHost = {
    withHttpServer { httpServer =>
      val awsRequest = AwsHttpRequest(
        AwsHost("unknownhost"),
        RequestBody("{}"),
        List())

      Await.result(HttpClient.httpRequest(awsRequest).toEither, 10 seconds) fold(
        err => err should be_==(UnreachableHostError("unknownhost")),
        succ => ko(s"The test is expected to fail but succeeded instead got $succ")
        )
    }
  }

  def invalidHost = {
    val awsRequest = AwsHttpRequest(
      AwsHost("  "),
      RequestBody("{}"),
      List())

    Await.result(HttpClient.httpRequest(awsRequest).toEither, 10 seconds) fold(
      err => err should be_==(InvalidHostError("  ")),
      succ => ko(s"The test is expected to fail but succeeded instead got $succ")
      )
  }

  def invalidResponse = {
    withHttpServer { httpServer =>
      val awsRequest = AwsHttpRequest(
        AwsHost(s"localhost:${httpServer.httpsPort()}/"),
        RequestBody("{}"),
        List())

      httpServer.stubFor {
        post(urlEqualTo("/"))
          .withRequestBody(equalToJson("{}"))
          .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE))
      }

      Await.result(HttpClient.httpRequest(awsRequest).toEither, 10 seconds) fold(
        err => err.isInstanceOf[UnexpectedDynamoError] should be_==(true),
        succ => ko(s"The test is expected to fail but succeeded instead got $succ")
        )
    }
  }

}
