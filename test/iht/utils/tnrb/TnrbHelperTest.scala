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

package iht.utils.tnrb

import akka.japi.Option.Some
import iht.FakeIhtApp
import iht.constants.IhtProperties
import iht.controllers.application.tnrb.routes
import iht.models.application.tnrb.WidowCheck
import iht.testhelpers._
import iht.testhelpers.ContentChecker
import iht.utils.CommonHelper._
import org.joda.time.LocalDate
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import iht.testhelpers.TestHelper._
import play.api.i18n.{Messages, MessagesApi}

/**
 *
 * Created by Vineet Tyagi on 28/05/15.
 *
 * This Class contains the Unit Tests for iht.utils.tnrb.TnrbHelper
 */
class TnrbHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  lazy val spouseOrCivilPartnerFirstName = CommonBuilder.firstNameGenerator
  lazy val spouseOrCivilPartnerLastName = CommonBuilder.surnameGenerator

  val deceasedName = CommonBuilder.firstNameGenerator

  "previousSpouseOrCivilPartner" must {
    "return previous spouse or civil partner name when it exists" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.previousSpouseOrCivilPartner(Some(tnrbModel), Some(widowCheck), deceasedName)
      result should be(tnrbModel.Name.toString)
    }

    "return \"dd's previous spouse or civil partner\" when no name exists and date after cp date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.previousSpouseOrCivilPartner(Some(tnrbModel), Some(widowCheck), deceasedName)
      result should be(s"$deceasedName’s previous spouse or civil partner")
    }

    "return \"dd's previous spouse\" when no name exists and date before cp date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = TnrbHelper.previousSpouseOrCivilPartner(Some(tnrbModel), Some(widowCheck), deceasedName)
      result should be(s"$deceasedName’s previous spouse")
    }
  }

  "mutateContent" must {
    "not mutate when english" in {
      val content = "abc gan priod ghi"
      TnrbHelper.mutateContent(content, "en") shouldBe content
    }

    "mutate when welsh" in {
      val content = "abc gan priod ghi"
      TnrbHelper.mutateContent(content, "cy") shouldBe "abc gan briod ghi"
    }
  }

  "vowelConsciousAnd" must {

    "always return 'and' when English language is selected" in {
      TnrbHelper.vowelConsciousAnd("John Smith", "en") shouldBe "page.iht.application.tnrbEligibilty.partner.additional.label.and"
    }

    "return 'ac' when Welsh language is selected and the predeceased name starts with a vowel" in {
      TnrbHelper.vowelConsciousAnd("Anne Smith", "cy") shouldBe "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterVowel"
      TnrbHelper.vowelConsciousAnd("Yvonne Smith", "cy") shouldBe "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterVowel"
    }

    "return 'a' when Welsh language is selected and the predeceased name starts with a consonant" in {
      TnrbHelper.vowelConsciousAnd("John Smith", "cy") shouldBe "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterConsonant"
      TnrbHelper.vowelConsciousAnd("Sarah Smith", "cy") shouldBe "page.iht.application.tnrbEligibilty.partner.additional.label.andAfterConsonant"
    }

  }

  "spouseOrCivilPartnerLabelGenitive" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerLabelGenitive(tnrbModel, widowCheck)
      result should be(spouseOrCivilPartnerFirstName + " " + spouseOrCivilPartnerLastName)
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerLabelGenitive(tnrbModel, widowCheck, "prefix")
      result should be("prefix’s " + messagesApi(spouseOrCivilPartnerMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = TnrbHelper.spouseOrCivilPartnerLabelGenitive(tnrbModel, widowCheck, "prefix")
      result should be("prefix’s " + messagesApi(spouseMessageKey))
    }
  }

  "spouseOrCivilPartnerNameLabel" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerNameLabel(tnrbModel, widowCheck)
      result should be(messagesApi("iht.name.upperCaseInitial"))
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.spouseOrCivilPartnerNameLabel(tnrbModel, widowCheck, "prefix")
      result should be("prefix " + messagesApi(spouseOrCivilPartnerMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = TnrbHelper.spouseOrCivilPartnerNameLabel(tnrbModel, widowCheck, "prefix")
      result should be("prefix " + messagesApi(spouseMessageKey))
    }
  }

  "preDeceasedMaritalStatusLabel" must {
    "return spouse or CivilPartner name when name has been entered" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName))
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.preDeceasedMaritalStatusLabel(tnrbModel, widowCheck)
      result should be(spouseOrCivilPartnerFirstName + " " + spouseOrCivilPartnerLastName + " " + messagesApi(marriedMessageKey))
    }

    "return prefix plus spouse or CivilPartner message when name has not been entered and date of death is after " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDatePlusOne))
      val result = TnrbHelper.preDeceasedMaritalStatusLabel(tnrbModel, widowCheck)
      result should be(messagesApi("iht.the.deceased") + " " + messagesApi(marriedOrInCivilPartnershipMessageKey))
    }

    "return prefix plus spouse message when name has not been entered and date of death is before " +
      "Civil Partnership Inclusion date" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = None, lastName = None)
      val widowCheck = CommonBuilder.buildWidowedCheck copy (dateOfPreDeceased = Some(civilPartnershipExclusionDateMinusOne))
      val result = TnrbHelper.preDeceasedMaritalStatusLabel(tnrbModel, widowCheck)
      result should be(messagesApi("iht.the.deceased") + " " +
        messagesApi(marriedMessageKey))
    }
  }

  "spouseOrCivilPartnerMessage" must {
    "return spouse message as the date is before Civil Partnership Inclusion date" in {
      val result = TnrbHelper.spouseOrCivilPartnerMessage(Some(civilPartnershipExclusionDateMinusOne))
      result should be("spouse")
    }

    "return spouse or CivilPartner message as the date is equal to Civil Partnership Inclusion date" in {
      val result = TnrbHelper.spouseOrCivilPartnerMessage(Some(civilPartnershipExclusionDate))
      result should be("spouse or civil partner")
    }

    "return spouse or CivilPartner message as the date is after Civil Partnership Inclusion date" in {
      val result = TnrbHelper.spouseOrCivilPartnerMessage(Some(civilPartnershipExclusionDatePlusOne))
      result should be("spouse or civil partner")
    }
  }

  "preDeceasedMaritalStatusSubLabel" must {
    "return spouse message as the date is before Civil Partnership Inclusion date" in {
      val result = TnrbHelper.preDeceasedMaritalStatusSubLabel(Some(civilPartnershipExclusionDateMinusOne))
      result should be("married")
    }

    "return spouse or CivilPartner message as the date is equal to Civil Partnership Inclusion date" in {
      val result = TnrbHelper.preDeceasedMaritalStatusSubLabel(Some(civilPartnershipExclusionDate))
      result should be("married or in a civil partnership")
    }

    "return spouse or CivilPartner message as the date is after Civil Partnership Inclusion date" in {
      val result = TnrbHelper.preDeceasedMaritalStatusSubLabel(Some(civilPartnershipExclusionDatePlusOne))
      result should be("married or in a civil partnership")
    }
  }

  "marriageOrCivilPartnerShipLabelForPdf" must {
    "return spouse message as the date is before Civil Partnership Inclusion date" in {
      val result = TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(Some(civilPartnershipExclusionDateMinusOne))
      result should be(messagesApi("page.iht.application.tnrbEligibilty.partner.marriage.label"))
    }

    "return spouse or CivilPartner message as the date is equal to Civil Partnership Inclusion date" in {
      val result = TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(Some(civilPartnershipExclusionDate))
      result should be(messagesApi("page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label"))
    }

    "return spouse or CivilPartner message as the date is after Civil Partnership Inclusion date" in {
      val result = TnrbHelper.marriageOrCivilPartnerShipLabelForPdf(Some(civilPartnershipExclusionDatePlusOne))
      result should be(messagesApi("page.iht.application.tnrbEligibilty.partner.marriageOrCivilPartnership.label"))
    }
  }

  "successfulTnrbRedirect" must {
    "redirect to Tnrb success page if it matches the happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy(firstName = Some(spouseOrCivilPartnerFirstName), lastName = Some(spouseOrCivilPartnerLastName),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))
      val result = TnrbHelper.successfulTnrbRedirect(applicationDetails)
      val actualResult: Option[String] = redirectLocation(result)
      val expectedResult: Option[String] = Some(routes.TnrbSuccessController.onPageLoad().url)
      actualResult should be(expectedResult)
    }

    "redirect to Tnrb overview page if it does not match the happy path" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(increaseIhtThreshold =
        Some(CommonBuilder.buildTnrbEligibility.copy( isPartnerLivingInUk=Some(false),
          dateOfMarriage= Some(new LocalDate(1984, 12, 11)))),
        widowCheck = Some(CommonBuilder.buildWidowedCheck))
      val result = TnrbHelper.successfulTnrbRedirect(applicationDetails)

      val actualResult: Option[String] = redirectLocation(result)
      val expectedResult: Option[String] = Some(routes.TnrbOverviewController.onPageLoad().url)
      actualResult should be(expectedResult)
    }
  }

  "cancelLinkUrlForWidowCheckPages" must {
    "return Call to EstateOverviewController if widow check date empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(ihtRef=Some(ihtRef),
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=None)))
      val expectedResult = iht.controllers.application.routes.EstateOverviewController.onPageLoadWithIhtRef(ihtRef)
      val result = TnrbHelper.cancelLinkUrlForWidowCheckPages(ad)
      result shouldBe expectedResult
    }

    "return Call to TnrbOverviewController if widow check date is not empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=Some(LocalDate.now()))))
      val expectedResult = iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad
      val result = TnrbHelper.cancelLinkUrlForWidowCheckPages(ad)
      result shouldBe expectedResult
    }
  }

  "cancelLinkTextForWidowCheckPages" must {
    "return \"Return to estate overview\" if widow check date empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(ihtRef=Some(ihtRef),
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=None)))
      val expectedResult = messagesApi("iht.estateReport.returnToEstateOverview")
      val result = TnrbHelper.cancelLinkTextForWidowCheckPages(ad)
      result shouldBe expectedResult
    }

    "return \"Return to increasing the threshold\" if widow check date is not empty" in {
      val ihtRef = "ihtRef"
      val ad = CommonBuilder.buildApplicationDetails.copy(
        widowCheck= Some(CommonBuilder.buildWidowedCheck copy(dateOfPreDeceased=Some(LocalDate.now()))))
      val expectedResult = messagesApi("page.iht.application.tnrb.returnToIncreasingThreshold")
      val result = TnrbHelper.cancelLinkTextForWidowCheckPages(ad)
      result shouldBe expectedResult
    }
  }

  "getEntryPointForTnrb" must {
    "return WidowedCheck question page url when marital status is other than single and widowed and" +
      "widowcheck question has not been answered" in {
      val regDetailsDeceasedMarried = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some(TestHelper.MaritalStatusMarried))))

      val appDetails = CommonBuilder.buildApplicationDetails

      TnrbHelper.getEntryPointForTnrb(regDetailsDeceasedMarried, appDetails) shouldBe
        iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad()

    }
  }

  "getEntryPointForTnrb" must {
    "return Tnrb Overview url when marital status is other than single and widowed and" +
      "widowcheck date question has been answered" in {
      val regDetailsDeceasedMarried = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
          maritalStatus = Some(TestHelper.MaritalStatusMarried))))

      val appDetails = CommonBuilder.buildApplicationDetails.copy(widowCheck = Some(CommonBuilder.buildWidowedCheck))

      TnrbHelper.getEntryPointForTnrb(regDetailsDeceasedMarried, appDetails) shouldBe
        iht.controllers.application.tnrb.routes.TnrbOverviewController.onPageLoad()

    }
  }

  "getEntryPointForTnrb" must {
    "return WidowedCheck date question page url when marital status widowed and" +
      "widowcheck date question has not been answered" in {
      val regDetailsDeceasedWidowed = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(
          maritalStatus = Some(TestHelper.MaritalStatusWidowed))))

      val appDetails = CommonBuilder.buildApplicationDetails

      TnrbHelper.getEntryPointForTnrb(regDetailsDeceasedWidowed, appDetails) shouldBe
        iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad()

    }
  }

  "getEntryPointForTnrb" must {
    "throw RunTime exception if Marital status is not known" in {
      val regDetailsDeceasedWidowed = CommonBuilder.buildRegistrationDetails.copy(
        deceasedDetails = Some(CommonBuilder.buildDeceasedDetails.copy(maritalStatus = Some("InCorrect"))))

      val appDetails = CommonBuilder.buildApplicationDetails

      intercept[RuntimeException] {
        TnrbHelper.getEntryPointForTnrb(regDetailsDeceasedWidowed, appDetails)
      }
    }
  }

  "urlForIncreasingThreshold" must {
    "return deceasedWidowCheckDatePage if Marital status is widowed" in {

     TnrbHelper.urlForIncreasingThreshold(IhtProperties.statusWidowed) should be
      iht.controllers.application.tnrb.routes.DeceasedWidowCheckDateController.onPageLoad()
    }
  }

  "urlForIncreasingThreshold" must {
    "return deceasedWidowCheckDatePage if Marital status is either Divorced or Married" in {

      TnrbHelper.urlForIncreasingThreshold(IhtProperties.statusWidowed) should be
      iht.controllers.application.tnrb.routes.DeceasedWidowCheckQuestionController.onPageLoad()
    }
  }

  "urlForIncreasingThreshold" must {
    "throw RunTime exception if Marital status is not known" in {
     val maritalStatus = "NOt_Known"
      intercept[RuntimeException] {
        TnrbHelper.urlForIncreasingThreshold(maritalStatus)
      }

    }
  }

  "spouseOrCivilPartnerName" must {
    "return the spouse name" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility copy(firstName = Some(spouseOrCivilPartnerFirstName),
                                                              lastName = Some(spouseOrCivilPartnerLastName))

      ContentChecker.stripLineBreaks(TnrbHelper.spouseOrCivilPartnerName(tnrbModel, "pretext")) shouldBe
                      spouseOrCivilPartnerFirstName+" "+spouseOrCivilPartnerLastName
    }
    "return the pretext string when there is no spouse name" in {
      val tnrbModel = CommonBuilder.buildTnrbEligibility.copy(firstName = None, lastName = None)

      TnrbHelper.spouseOrCivilPartnerName(tnrbModel, "pretext") shouldBe "pretext"
    }
  }

}
