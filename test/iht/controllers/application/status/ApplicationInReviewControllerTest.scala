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

package iht.controllers.application.status

import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.views.HtmlSpec
import uk.gov.hmrc.play.partials.FormPartialRetriever

class ApplicationInReviewControllerTest extends ApplicationControllerTest with HtmlSpec {

  "ApplicationInReviewController" must {
    "implement the correct view" in {
      val deceasedName = "Xyz"
      val request = createFakeRequest()
      implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
      val pageContent = ApplicationInReviewController.getView("",deceasedName,CommonBuilder.buildProbateDetails)(request, formPartialRetriever).toString
      titleShouldBeCorrect(pageContent, messagesApi("page.iht.application.overview.inreview.title", deceasedName))
    }
  }
}
