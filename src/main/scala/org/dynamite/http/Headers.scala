package org.dynamite.http

import org.dynamite.dsl.{AwsAuthorization, DateStamp}

trait HttpHeader {
  def render: (String, String)
}

case class AcceptEncodingHeader(value: String) extends HttpHeader {
  def render = "Accept-Encoding" -> value
}

case class ContentTypeHeader(contentType: String) extends HttpHeader {
  def render = "Content-Type" -> contentType
}

//see http://docs.aws.amazon.com/general/latest/gr/sigv4-calculate-signature.html
case class AuthorizationHeader(value: AwsAuthorization) extends HttpHeader {
  def render = "Authorization" -> s"AWS4-HMAC-SHA256 Credential=${value.credential}, SignedHeaders=content-type;host;x-amz-date, Signature=${value.signature}"
}

case class AmazonDateHeader(dateStamp: DateStamp) extends HttpHeader {
  def render = "X-Amz-Date" -> dateStamp.value
}

case class AmazonTargetHeader(value: String) extends HttpHeader {
  def render = "X-Amz-Target" -> value
}

case class HostHeader(value: String) extends HttpHeader {
  def render = "host" -> value
}