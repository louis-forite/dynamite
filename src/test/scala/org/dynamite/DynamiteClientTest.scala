package org.dynamite

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching._
import org.dynamite.ast.S
import org.dynamite.dsl._
import org.dynamite.http.HttpServer
import org.specs2.Specification

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class Dummy(id: String, test: String)

class DynamiteClientTest extends Specification with HttpServer {
  def is =
    s2"""
      Specifications for the DynamiteClient
        Getting an existing item should return this item $get
        Getting a non existing item should return None $getNotFound
        Put an existing item should return success $put
  """

  def get = withHttpServer { httpServer =>
    val host: AwsHost = AwsHost(s"localhost:${httpServer.httpsPort()}")
    val client = setupClient(host)

    httpServer.stubFor {
      post(urlEqualTo("/"))
        .withHeader("Accept-Encoding", new EqualToPattern("identity"))
        .withHeader("Content-Type", new EqualToPattern("application/x-amz-json-1.0"))
        .withHeader("X-Amz-Date", new AnythingPattern(""))
        .withHeader("Host", new EqualToPattern(host.value))
        .withHeader("X-Amz-Target", new EqualToPattern("DynamoDB_20120810.GetItem"))
        .withHeader("Authorization", new ContainsPattern("Credential"))
        .withRequestBody(equalToJson("""{"ConsistentRead":false,"Key":{"id":{"S":"id"}},"TableName":"dummy_table"}"""))
        .willReturn {
          aResponse()
            .withStatus(200)
            .withBody("""{"Item" : {"id" : { "S" : "id"}, "test" : { "S" : "test_value"}}}""")
        }
    }

    val result = Await.result(client.get[Dummy]("id" -> S("id")), 10 seconds)
    result must be_==(Right(GetItemResult(Some(Dummy("id", "test_value")))))
  }

  def getNotFound = withHttpServer { httpServer =>
    val host: AwsHost = AwsHost(s"localhost:${httpServer.httpsPort()}")
    val client = setupClient(host)

    httpServer.stubFor {
      post(urlEqualTo("/"))
        .withHeader("Accept-Encoding", new EqualToPattern("identity"))
        .withHeader("Content-Type", new EqualToPattern("application/x-amz-json-1.0"))
        .withHeader("X-Amz-Date", new AnythingPattern(""))
        .withHeader("Host", new EqualToPattern(host.value))
        .withHeader("X-Amz-Target", new EqualToPattern("DynamoDB_20120810.GetItem"))
        .withHeader("Authorization", new ContainsPattern("Credential"))
        .withRequestBody(equalToJson("""{"ConsistentRead":false,"Key":{"id":{"S":"id"}},"TableName":"dummy_table"}"""))
        .willReturn {
          aResponse()
            .withStatus(200)
            .withBody("""{}""")
        }
    }

    Await.result(client.get[Dummy]("id" -> S("id")), 10 seconds) must be_==(Right(GetItemResult(None)))
  }

  def put = withHttpServer { httpServer =>
    val host: AwsHost = AwsHost(s"localhost:${httpServer.httpsPort()}")
    val client = setupClient(host)

    httpServer.stubFor {
      post(urlEqualTo("/"))
        .withHeader("Accept-Encoding", new EqualToPattern("identity"))
        .withHeader("Content-Type", new EqualToPattern("application/x-amz-json-1.0"))
        .withHeader("X-Amz-Date", new AnythingPattern(""))
        .withHeader("Host", new EqualToPattern(host.value))
        .withHeader("X-Amz-Target", new EqualToPattern("DynamoDB_20120810.PutItem"))
        .withHeader("Authorization", new ContainsPattern("Credential"))
        .withRequestBody(equalToJson("""{"Item":{"id":{"S":"id"},"test":{"S":"test_value"}},"TableName":"dummy_table"}"""))
        .willReturn {
          aResponse()
            .withStatus(200)
            .withBody("""{}""")
        }
    }

    Await.result(client.put(Dummy("id", "test_value")), 10 seconds) must be_==(Right(PutItemResult()))
  }


  private def setupClient(host: AwsHost) = {
    val region = new AwsRegion {
      lazy val name = AwsRegionName("test-region")
      lazy val endpoint = host
    }
    val configuration = ClientConfiguration(AwsTable("dummy_table"), region)
    val credentials = AwsCredentials(AwsAccessKey("test"), AwsSecretKey("test"))
    DynamiteClient(configuration, credentials)
  }
}