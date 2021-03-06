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

package iht.views.application.tnrb

import iht.views.ViewTestHelper
import iht.views.html.application.tnrb.tnrb_overview_table_row
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Call
import play.twirl.api.Html
import iht.constants.Constants._
import iht.constants.IhtProperties._

class TnrbOverviewTableRowViewTest extends ViewTestHelper {

  lazy val id = "home-in-uk"
  lazy val questionText = "Sample question"
  lazy val questionScreenReaderText = "Sample screen reader"
  lazy val questionCategory = "questionAnswer"
  lazy val link = iht.controllers.application.tnrb.routes.PermanentHomeController.onPageLoad()
  lazy val answerValue = "Sample value"
  lazy val linkID = TnrbSpousePermanentHomeInUKID

  def tnrbOverviewTableRow(id: String = "home-in-uk",
                           questionText:Html = Html("Sample question"),
                           questionScreenReaderText: String = "Sample screen reader",
                           questionCategory:String = "questionAnswer",
                           answerValue:String = "Sample value",
                           answerValueFormatted:Option[Html] = None,
                           link:Option[Call] = None,
                           linkScreenReader:String = "",
                           linkID: String = TnrbSpousePermanentHomeInUKID
                          ) =  {

    implicit val request = createFakeRequest()
      val view = tnrb_overview_table_row(id,
          questionText,
          questionScreenReaderText,
          questionCategory,
          answerValue,
          answerValueFormatted,
          link:Option[Call],
          linkScreenReader,
          linkID
      ).toString

     asDocument(view)
  }

  "TnrbOverviewTableRow" must {

    "have no message keys in html" in {
      noMessageKeysShouldBePresent(tnrbOverviewTableRow().toString)
    }

    "have the correct id" in {
      val view = tnrbOverviewTableRow()
      assertRenderedById(view, id)
    }

    "have the correct question text" in {
      val view = tnrbOverviewTableRow()
      assertRenderedById(view, s"$id-text")
    }

    "show the value if it has" in {
      val view = tnrbOverviewTableRow()

      val value = view.getElementById(s"$id-value")
      value.text shouldBe answerValue
    }

    "not show the value when there is not" in {
      val view = tnrbOverviewTableRow(answerValue = "")

      val value = view.getElementById(s"$id-value")
      value.text shouldBe empty
    }

    "show the correct link with text" in {
      val view = tnrbOverviewTableRow(link = Some(link))

      val questionLink = view.getElementById(s"$linkID")
      questionLink.attr("href") shouldBe link.url
      questionLink.text() shouldBe messagesApi("iht.change")
    }

    "show the correct question category when answer value is empty" in {
      val view = tnrbOverviewTableRow(answerValue = "", link = Some(link))

      val questionLink = view.getElementById(s"$linkID")
      questionLink.text() shouldBe messagesApi("site.link.giveAnswer")
    }
  }

}
