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

package iht.views.registration.kickout

import iht.views.ViewTestHelper
import play.api.i18n.Messages
import play.twirl.api.Html
import iht.views.html.registration.kickout.kickout_template
/**
  * Created by vineet on 15/11/16.
  */
class KickoutTemplateViewTest extends ViewTestHelper{

  val summaryMessage = "kickout summary message"
  val returnLinkUrl = iht.controllers.registration.deceased.routes.DeceasedDateOfDeathController.onPageLoad
  val seqOfContents = Seq("lineOne", "lineTwo")

  def kickOutTemplateView() = {
    implicit val request = createFakeRequest()

    val view = kickout_template(summaryMessage,
                        returnLinkUrl)(seqOfContents).toString()
    asDocument(view)
  }

  "KickoutTemplateView View" must {
    "have the correct title and summary message" in {
      val view = kickOutTemplateView()

      titleShouldBeCorrect(view.toString, Messages("iht.notPossibleToUseService"))
      messagesShouldBePresent(view.toString, summaryMessage)
    }

    "have 'Next steps' heading" in {
      val view = kickOutTemplateView
      val headers = view.getElementsByTag("h2")

      headers.first.text() shouldBe Messages("iht.nextSteps")
    }

    "have the sequence of contents" in {
      val view = kickOutTemplateView
      for (content <- seqOfContents) view.toString should include(content)

    }

    "have details are correct button " in {
      val view = kickOutTemplateView

      val detailsAreCorrectButton = view.getElementById("finish")
      detailsAreCorrectButton.attr("value") shouldBe Messages("site.button.details.correct.exitToGovK")
    }

    "have return link with correct text" in {
      val view = kickOutTemplateView

      val detailsAreCorrectButton = view.getElementById("return-button")
      detailsAreCorrectButton.attr("href") shouldBe returnLinkUrl.url
      detailsAreCorrectButton.text shouldBe Messages("iht.registration.kickout.message.returnToLast")
    }
  }

}
