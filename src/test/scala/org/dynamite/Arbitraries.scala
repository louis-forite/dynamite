package org.dynamite

import org.json4s.JsonAST._
import org.scalacheck.Gen.{listOf, oneOf}
import org.scalacheck.{Arbitrary, Gen}
import Arbitrary.arbitrary

object Arbitraries {

  implicit val jsObjectArbitrary = Arbitrary {
    for {
      fields <- listOf(fieldArbitrary)
    } yield JObject(fields)
  }

  def fieldArbitrary: Gen[JField] =
    for {
      field <- arbitrary[String]
      value <- jsValueGen
    } yield (field, value)

  def jsValueGen: Gen[JValue] = oneOf(
    arbitrary[String].map(JString(_)),
    arbitrary[Int].map(JInt(_)),
    arbitrary[Double].map(JDecimal(_)),
    arbitrary[Long].map(JLong(_)),
    arbitrary[Boolean].map(JBool(_)),
    listOf(arbitrary[String]).map(jstrings => JArray(jstrings map JString)))
}
