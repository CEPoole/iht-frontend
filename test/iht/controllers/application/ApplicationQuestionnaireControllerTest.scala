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

package iht.controllers.application

import iht.connector.{CachingConnector, ExplicitAuditConnector, IhtConnector}
import iht.constants.{Constants, IhtProperties}
import iht.models.QuestionnaireModel
import iht.testhelpers.MockFormPartialRetriever
import iht.utils.{CommonHelper, IhtSection}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

/**
 * Created by yasar on 6/18/15.
 */
class ApplicationQuestionnaireControllerTest extends ApplicationControllerTest {

  override implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val hc = new HeaderCarrier()
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]
  val mockAuditConnector = mock[ExplicitAuditConnector]

  // Create controller object and pass in mock.
  def questionnaireController = new ApplicationQuestionnaireController {
    override val authConnector = createFakeAuthConnector()
    override def explicitAuditConnector = mockAuditConnector
    def cachingConnector = mockCachingConnector
    def ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def questionnaireControllerNotAuthorised = new ApplicationQuestionnaireController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override def explicitAuditConnector = mockAuditConnector
    def cachingConnector = mockCachingConnector
    def ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "onApplicationPageLoad method" must {
    "respond with OK and correct header title on page load" in {

      val result = questionnaireController.onPageLoad()(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include(messagesApi("site.application.title"))
    }

    "respond with no intent question on page load" in {
      val result = questionnaireController.onPageLoad()(createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should not include messagesApi("page.iht.questionnaire.intendReturn.question")
    }

    "redirect to questionnaire page when Nino is present in the session" in {
      val result = questionnaireController.onPageLoad()(createFakeRequest().withSession(Constants.NINO -> "CSXXXXX"))
      status(result) shouldBe OK
    }

    "redirect to Case List page when Nino is not present in the session" in {
      val result = questionnaireController.onPageLoad()(createFakeRequest(false).withSession())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad().url)
    }

    "respond with redirect on page submit" in {
      val result = questionnaireController.onSubmit()(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(IhtProperties.linkGovUkIht))
    }

    "set up instance for explicit audit connector" in {
      ApplicationQuestionnaireController.explicitAuditConnector shouldBe ExplicitAuditConnector
    }

    "log helper bad request validation" in {
      val questionnaireModel = QuestionnaireModel(Some(7),None,None,None,None,None,None)
      val questionnaire_form1 = iht.forms.QuestionnaireForms.questionnaire_form.fill(questionnaireModel)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(questionnaire_form1.data.toSeq: _*)

      val result = questionnaireController.onSubmit()(request)
      status(result) should be(BAD_REQUEST)
    }

    "have the correct iht section" in {
      questionnaireController.ihtSection shouldBe IhtSection.Application
    }

    "have the correct callPageLoad" in {
      questionnaireController.callPageLoad shouldBe
        iht.controllers.application.routes.ApplicationQuestionnaireController.onPageLoad()
    }
  }

}
