/*
 * Copyright 2017 HM Revenue & Customs
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

package iht.views.application.assets.pensions

import iht.controllers.application.assets.pensions.routes
import iht.models.application.assets.PrivatePension
import iht.testhelpers.CommonBuilder
import iht.utils.CommonHelper
import iht.views.ViewTestHelper
import iht.views.helpers.GenericOverviewHelper._
import iht.views.html.application.asset.pensions.pensions_overview
import play.api.i18n.Messages
import iht.constants.Constants._

class PensionsOverviewViewTest extends ViewTestHelper {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  lazy val valueYes = "Yes"
  lazy val valueNo = "No"
  lazy val changeMsgKey = "iht.change"
  lazy val giveAnAnswerMsgKey= "site.link.giveAnswer"
  lazy val giveAValueMsgKey = "site.link.giveAValue"
  lazy val pensionOwnedByDeceasedQuestionId = "pensions-question"
  lazy val pensionOwnedByDeceasedQuestionMsg = Messages("page.iht.application.assets.pensions.question", deceasedName)
  lazy val pensionOwnedByDeceasedQuestionPageUrl = routes.PensionsOwnedQuestionController.onPageLoad.url

  lazy val pensionMoreThanOneQuestionId = "pensions-more-than-one-question"
  lazy val pensionMoreThanOneQuestionMsg = Messages("page.iht.application.assets.pensions.changed.title", deceasedName)
  lazy val pensionMoreThanOneQuestionPageUrl = routes.PensionsChangedQuestionController.onPageLoad.url

  lazy val pensionValueQuestionId = "pensions-value"
  lazy val pensionValueQuestionMsg = Messages("iht.estateReport.assets.pensions.valueOfRemainingPaymentsBeingPaid")
  lazy val pensionValueQuestionPageUrl = routes.PensionsValueController.onPageLoad.url

  def pensionOverviewView(pensions:Option[PrivatePension]) = {
    implicit val request = createFakeRequest()

    val view = pensions_overview(pensions, regDetails).toString()
    asDocument(view)
  }

  "PensionsOverview view" must {

    "have correct title and browser title " in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended)).toString

      titleShouldBeCorrect(view, Messages("iht.estateReport.assets.privatePensions"))
      browserTitleShouldBeCorrect(view, Messages("iht.estateReport.assets.privatePensions"))
    }

    "have correct guidance paragraphs" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended)).toString
      messagesShouldBePresent(view, Messages("page.iht.application.assets.pensions.overview.description.p1",
                                            deceasedName, deceasedName))
      messagesShouldBePresent(view, Messages("page.iht.application.assets.pensions.overview.description.p2",
                                              deceasedName))
    }

    "have return link with correct text" in  {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended))

      val link = view.getElementById("return-button")
      link.text shouldBe Messages("page.iht.application.return.to.assetsOf", deceasedName)
      link.attr("href") shouldBe iht.controllers.application.assets.routes.AssetsOverviewController.onPageLoad.url + "#" + AppSectionPrivatePensionID
    }

  }

  "Did the deceased have any private pensions section" must {
    "have correct question, link and value" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(isOwned = Some(true))))

      rowShouldBeAnswered(view,
        pensionOwnedByDeceasedQuestionId,
        pensionOwnedByDeceasedQuestionMsg,
        valueYes,
        changeMsgKey,
        pensionOwnedByDeceasedQuestionPageUrl)
    }

  }

 "Unanswered - Private pensions change in 2 years and Pension value sections" must {

    "have the correct question, link and value for 'Private pensions change in 2 years' section" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(isOwned = Some(true))))

      rowShouldBeUnAnswered(view,
        pensionMoreThanOneQuestionId,
        pensionMoreThanOneQuestionMsg,
        giveAnAnswerMsgKey,
        pensionMoreThanOneQuestionPageUrl
      )
    }

   "have the correct question, link and value for 'Pension value' section" in {
     val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(isOwned = Some(true))))

     rowShouldBeUnAnswered(view,
       pensionValueQuestionId,
       pensionValueQuestionMsg,
       giveAValueMsgKey,
       pensionValueQuestionPageUrl
     )
    }
  }

  "Answered with Yes - Private pensions change in 2 years and Pension value sections" must {

    "have the correct question, link and value for 'Private pensions change in 2 years' section" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(
        isOwned = Some(true),isChanged = Some(true)))
      )

      rowShouldBeAnswered(view,
        pensionMoreThanOneQuestionId,
        pensionMoreThanOneQuestionMsg,
        valueYes,
        changeMsgKey,
        pensionMoreThanOneQuestionPageUrl
      )
    }

    "have the correct question, link and value for 'Pension value' section" in {
      val pensionValue = BigDecimal(1000)
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(
        isOwned = Some(true),value = Some(pensionValue)
      )))

      rowShouldBeAnswered(view,
        pensionValueQuestionId,
        pensionValueQuestionMsg,
        "£" + CommonHelper.numberWithCommas(pensionValue),
        changeMsgKey,
        pensionValueQuestionPageUrl
      )
    }
  }

  "Answered with No - Private pensions change in 2 years and Pension value sections" must {

    "have the correct question, link and value for 'Private pensions change in 2 years' section" in {
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(
        isOwned = Some(true),isChanged = Some(false)))
      )

      rowShouldBeAnswered(view,
        pensionMoreThanOneQuestionId,
        pensionMoreThanOneQuestionMsg,
        valueNo,
        changeMsgKey,
        pensionMoreThanOneQuestionPageUrl
      )
    }

    "have the correct question, link and value for 'Pension value' section" in {
      val pensionValue = BigDecimal(1000)
      val view = pensionOverviewView(Some(CommonBuilder.buildPrivatePensionExtended.copy(
        isOwned = Some(true))))

      rowShouldBeAnswered(view,
        pensionValueQuestionId,
        pensionValueQuestionMsg,
        "",
        giveAValueMsgKey,
        pensionValueQuestionPageUrl
      )
    }
  }

}
