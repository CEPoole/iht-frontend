/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package iht.utils

import iht.FakeIhtApp
import iht.constants.FieldMappings
import iht.forms.FormTestHelper
import iht.testhelpers.{CommonBuilder, NinoBuilder, TestHelper}
import iht.utils.IhtFormValidator._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.data.{FieldMapping, Form, FormError}
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.test.UnitSpec

import scala.collection.immutable.ListMap

/**
 * Created by xavierzanatta on 3/20/15.
 */
class FormValidatorTest extends  FormTestHelper with FakeIhtApp {

  val nino = CommonBuilder.DefaultNino

  "isNotFutureDate" must {
    "return false the date is later than current date" in {
      val testDate = LocalDate.now().plusDays(1)
      val result = IhtFormValidator.isNotFutureDate(testDate)
      result should be(false)
    }

    "return true the date is equal to current date" in {
      val testDate = LocalDate.now()
      val result = IhtFormValidator.isNotFutureDate(testDate)
      result should be(true)
    }

    "return true the date is earlier than current date" in {
      val testDate = LocalDate.now().minusDays(1)
      val result = IhtFormValidator.isNotFutureDate(testDate)
      result should be(true)
    }
  }

  "validateCountryCode" must {
    "reject an invalid country code" in {
      IhtFormValidator.validateCountryCode("UK") should be(false)
    }
    "accept a valid country code" in {
      IhtFormValidator.validateCountryCode("GB") should be(true)
    }
  }

  "existsInKeys" must {
    "return true if valid list map key" in {
      val result = IhtFormValidator.existsInKeys(TestHelper.MaritalStatusSingle, FieldMappings.maritalStatusMap(messages))
      result should be(true)
    }

    "return false if invalid list map key" in {
      val result = IhtFormValidator.existsInKeys("7", FieldMappings.maritalStatusMap(messages))
      result should be(false)
    }
  }

  "currency" should {
    "Report correctly for invalid numeric value length>10" in {
      optionalCurrencyWithoutFieldName.bind(Map("" -> "11111111111111111111")) shouldBe Left(List(FormError("", "error.currencyValue.length")))
    }
 }

  "mandatoryPhoneNumberFormatter" should {
    "Return expected mapping validation for various inputs, valid and invalid" in {
      import play.api.data.FormError

      val formatter = mandatoryPhoneNumberFormatter("blank message", "invalid length", "invalid value")

      formatter.bind("a", Map("a" -> ""))  shouldBe Left(Seq(FormError("a", "blank message")))
      formatter.bind("a", Map("a" -> "1111111111111111111111111111"))  shouldBe Left(Seq(FormError("a", "invalid length")))
      formatter.bind("a", Map("a" -> "$5gggF"))  shouldBe Left(Seq(FormError("a", "invalid value")))
      formatter.bind("a", Map("a" -> "+44 020 1234 5678"))  shouldBe Right("0044 020 1234 5678")
      formatter.bind("a", Map("a" -> "(020) 1234 5678")) shouldBe Right("(020) 1234 5678")

      formatter.bind("a", Map("a" -> "(020) 1234 5678#1234")) shouldBe Right("(020) 1234 5678#1234")
      formatter.bind("a", Map("a" -> "(020) 1234 5678*6")) shouldBe Right("(020) 1234 5678*6")
      formatter.bind("a", Map("a" -> "(020) 1234 5678")) shouldBe Right("(020) 1234 5678")
      formatter.bind("a", Map("a" -> "02012345678")) shouldBe Right("02012345678")
      formatter.bind("a", Map("a" -> "02012345678 ext 1234")) shouldBe Right("02012345678 EXT 1234")
      formatter.bind("a", Map("a" -> "020123456+12 ext 1234")) shouldBe Left(Seq(FormError("a", "invalid value")))

    }
  }

  "phoneNumberOptionString" should {
    "Return expected mapping validation for various inputs, valid and invalid" in {
      import play.api.data.FormError
      val mapping: FieldMapping[Option[String]] = phoneNumberOptionString("blank message", "invalid length", "invalid value")

      mapping.bind(Map("" -> ""))  shouldBe Left(Seq(FormError("", "blank message")))
      mapping.bind(Map("" -> "1111111111111111111111111111"))  shouldBe Left(Seq(FormError("", "invalid length")))
      mapping.bind(Map("" -> "$5gggF"))  shouldBe Left(Seq(FormError("", "invalid value")))
      mapping.bind(Map("" -> "+44 020 1234 5678"))  shouldBe Right(Some("0044 020 1234 5678"))
      mapping.bind(Map("" -> "(020) 1234 5678")) shouldBe Right(Some("(020) 1234 5678"))

      mapping.bind(Map("" -> "(020) 1234 5678#1234")) shouldBe Right(Some("(020) 1234 5678#1234"))
      mapping.bind(Map("" -> "(020) 1234 5678*6")) shouldBe Right(Some("(020) 1234 5678*6"))
      mapping.bind(Map("" -> "(020) 1234 5678")) shouldBe Right(Some("(020) 1234 5678"))
      mapping.bind(Map("" -> "02012345678")) shouldBe Right(Some("02012345678"))
      mapping.bind(Map("" -> "02012345678 ext 1234")) shouldBe Right(Some("02012345678 EXT 1234"))
      mapping.bind(Map("" -> "020123456+12 ext 1234")) shouldBe Left(Seq(FormError("", "invalid value")))
    }
  }

  "ihtAddress" should {
    val allBlank = Map(
      "addr1key"->"",
      "addr2key"->"",
      "addr3key"->"",
      "addr4key"->"",
      "postcodekey"->"",
      "countrycodekey"->""
    )

    val first2Blank = Map(
      "addr1key"->"",
      "addr2key"->"",
      "postcodekey"->CommonBuilder.DefaultPostCode,
      "countrycodekey"->"GB"
    )

    val invalidLine2 = Map(
      "addr1key"->"addr1",
      "addr2key"->"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"pcode",
      "countrycodekey"->"GB"
    )

    val invalidChar1 = Map(
      "addr1key"->"<HTML>",
      "addr2key"->"addr2",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"pcode",
      "countrycodekey"->"GB"
    )

    val invalidChar2 = Map(
      "addr1key"->"addr1",
      "addr2key"->"<HTML>",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"pcode",
      "countrycodekey"->"GB"
    )

    val blankPostcode = Map(
      "addr1key"->"addr1",
      "addr2key"->"addr2",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"",
      "countrycodekey"->"GB"
    )

    val invalidPostcode = Map(
      "addr1key"->"addr1",
      "addr2key"->"addr2",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"CC!",
      "countrycodekey"->"GB"
    )

    val allowedBlankPostcode = Map(
      "addr1key"->"addr1",
      "addr2key"->"addr2",
      "addr3key"->"addr3",
      "addr4key"->"addr4",
      "postcodekey"->"",
      "countrycodekey"->"IL"
    )

    val formatter = ihtAddress("addr2key","addr3key","addr4key","postcodekey", "countrycodekey",
      "all-lines-blank","first-two-blank","invalid-line", "invalid-char", "blank-postcode","invalid-postcode", "blankcountrycode")

    "Return a formatter which responds suitably to all lines being blank" in {
      formatter.bind("", allBlank).left.get.contains(FormError("", "all-lines-blank")) shouldBe true
    }

    "Return a formatter which responds suitably to first two lines being blank" in {
      formatter.bind("", first2Blank).left.get.contains(FormError("", "all-lines-blank")) shouldBe true
    }
    "Return a formatter which responds suitably to invalid lines" in {
      formatter.bind("", invalidLine2).left.get.contains(FormError("addr2key", "invalid-line")) shouldBe true
    }

    "Return a formatter which responds suitably to blank postcode" in {
      formatter.bind("", blankPostcode).left.get.contains(FormError("postcodekey", "blank-postcode")) shouldBe true
    }
    "Return a formatter which responds suitably to invalid postcode" in {
      formatter.bind("", invalidPostcode).left.get.contains(FormError("postcodekey", "invalid-postcode")) shouldBe true
    }
    "Return a formatter which responds suitably to allowed country code" in {
      formatter.bind("", allowedBlankPostcode).left.get.contains(FormError("postcodekey", "blank-postcode")) shouldBe false
    }
  }

  "ihtRadio" should {
    val formatter = ihtRadio("no-selection", ListMap("a"->"a"))
    "Return a formatter which responds suitably to no item being selected" in {
      formatter.bind("radiokey", Map( "option1"->"option1" ))
        .left.get.contains(FormError("radiokey", "no-selection")) shouldBe true
    }
  }

  "radioOptionString" should {
    val formatter = radioOptionString("no-selection", ListMap("a"->"a"))
    "Return a formatter which responds suitably to no item being selected" in {
      formatter.bind("radiokey", Map( "option1"->"option1" ))
        .left.get.contains(FormError("radiokey", "no-selection")) shouldBe true
    }
  }

  "addDeceasedNameToAllFormErrors" must {
    "add the deceased name as the first argument to all form error objects against a form" in {
      val mapping: FieldMapping[Option[String]] = phoneNumberOptionString("blank message", "invalid length", "invalid value")
      val deceasedName = CommonBuilder.DefaultFirstName
      val errors = Seq(FormError("one", "message 1"), FormError("two", "message 2"))
      val f = Form(mapping, Map("" -> ""), errors, None)
      val result: Form[Option[String]] = addDeceasedNameToAllFormErrors(f, deceasedName)
      val actualOptionArgs1 = result.error("one").map(_.args)
      actualOptionArgs1 shouldBe Some(Seq(deceasedName))
      val actualOptionArgs2 = result.error("two").map(_.args)
      actualOptionArgs2 shouldBe Some(Seq(deceasedName))
    }
  }
}
