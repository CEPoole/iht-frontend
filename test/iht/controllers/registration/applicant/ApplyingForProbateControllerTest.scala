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

package iht.controllers.registration.applicant

import iht.connector.CachingConnector
import iht.forms.registration.ApplicantForms._
import iht.models.ApplicantDetails
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder, ContentChecker}
import iht.testhelpers.MockObjectBuilder._
import iht.utils.{DeceasedInfoHelper, RegistrationKickOutHelper}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

class ApplyingForProbateControllerTest
  extends RegistrationApplicantControllerWithEditModeBehaviour[ApplyingForProbateController] {

  before {
    mockCachingConnector = mock[CachingConnector]
  }

  // Create controller object and pass in mock.
  def controller = new ApplyingForProbateController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised=true)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def controllerNotAuthorised = new ApplyingForProbateController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = false)

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  "ApplyingForProbateController" must {

    behave like securedRegistrationApplicantController()

    "load when visited for the first time and show a Continue link" in {
      val regdDetailsWithDeceasedDetails = CommonBuilder.buildRegistrationDetailsWithDeceasedDetails
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(regdDetailsWithDeceasedDetails))

      val result = controller.onPageLoad(createFakeRequest())

      status(result) should be(OK)

      val contentResult = ContentChecker.stripLineBreaks(contentAsString(result))
      contentResult should include(messagesApi("page.iht.registration.applicant.applyingForProbate", DeceasedInfoHelper.getDeceasedNameOrDefaultString(regdDetailsWithDeceasedDetails)))
      contentResult should include(messagesApi("page.iht.registration.applicant.applyingForProbate.p1"))
      contentResult should include(messagesApi("page.iht.registration.applicant.applyingForProbate.p2", DeceasedInfoHelper.getDeceasedNameOrDefaultString(regdDetailsWithDeceasedDetails)))
      contentResult should include(messagesApi("iht.continue"))
      contentResult should not include(messagesApi("site.link.cancel"))
    }

    "load when revisited after answering Yes" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails =
          Some(new ApplicantDetails(isApplyingForProbate = Some(true))))))

      val result: Future[Result] = controller.onPageLoad(createFakeRequest())

      status(result) should be(OK)
    }

    "load when revisited after answering No" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails =
          Some(new ApplicantDetails(isApplyingForProbate = Some(false))))))

      val result = controller.onPageLoad(createFakeRequest())

      status(result) should be(OK)
    }

    "load in edit mode and show Continue and Cancel links" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy (applicantDetails =
          Some(new ApplicantDetails(isApplyingForProbate = Some(true))))))

      val result = controller.onEditPageLoad(createFakeRequest())

      status(result) should be(OK)

      contentAsString(result) should include(messagesApi("iht.continue"))
      contentAsString(result) should include(messagesApi("site.link.cancel"))
    }

    "redirect on load to the estate report page if the RegistrationDetails does not contain deceased details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val result = controller.onPageLoad(createFakeRequest())
      status(result) shouldBe SEE_OTHER
    }

    "redirect on submit to the estate report page if the RegistrationDetails does not contain deceased details" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetails))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(true)))
      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq)

      val result = controller.onSubmit(request)
      status(result) shouldBe SEE_OTHER
    }

    "show an error message on submit when the question is not answered" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = applyingForProbateForm.fill(ApplicantDetails()).data.toSeq
      val seq = form filter { case (key: String, value: String) => key != "isApplyingForProbate"}
      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = seq)

      val result = controller.onSubmit(request)
      status(result) should be(BAD_REQUEST)
      contentAsString(result) should include(messagesApi("error.applicantIsApplyingForProbate.select"))
    }

    "save and redirect correctly on submit when answering Yes" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(
          applicantDetails = Some(new ApplicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(true)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq)

      val result = controller.onSubmit(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(
        Some(iht.controllers.registration.applicant.routes.ProbateLocationController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.isApplyingForProbate shouldBe Some(true)
    }

    "save and redirect correctly on submit when answering No" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(
          applicantDetails = Some(new ApplicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))
      createMockToGetSingleValueFromCache(mockCachingConnector,
        singleValueReturn = Some(RegistrationKickOutHelper.KickoutNotApplyingForProbate))
      createMockToStoreSingleValueInCache(mockCachingConnector,
        singleValueReturn = Some(RegistrationKickOutHelper.KickoutNotApplyingForProbate))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(false)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL,
        host = host, data = form.data.toSeq)

      val result = controller.onSubmit(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.registration.routes.KickoutController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.isApplyingForProbate shouldBe Some(false)

      val storeResult = verifyAndReturnStoredSingleValue(mockCachingConnector)
      storeResult._1 shouldBe RegistrationKickOutHelper.RegistrationKickoutReasonCachingKey
      storeResult._2 shouldBe RegistrationKickOutHelper.KickoutNotApplyingForProbate
    }

    "save and redirect correctly on submit in edit mode when answering Yes" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(applicantDetails = Some(new ApplicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(true)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq)

      val result = controller.onEditSubmit(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.registration.routes.RegistrationSummaryController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.isApplyingForProbate shouldBe Some(true)
    }

    "show bad request when errors" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(applicantDetails = Some(new ApplicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector, Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(true)))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(("isApplyingForProbate", ""))
      val result = controller.onEditSubmit(request)
      status(result) should be(BAD_REQUEST)
    }

    "save and redirect correctly on submit in edit mode when answering No" in {
      createMockToGetRegDetailsFromCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails copy(applicantDetails = Some(new ApplicantDetails))))
      createMockToStoreRegDetailsInCache(mockCachingConnector,
        Some(CommonBuilder.buildRegistrationDetailsWithDeceasedDetails))
      createMockToGetSingleValueFromCache(mockCachingConnector,
        singleValueReturn = Some(RegistrationKickOutHelper.KickoutNotApplyingForProbate))
      createMockToStoreSingleValueInCache(mockCachingConnector,
        singleValueReturn = Some(RegistrationKickOutHelper.KickoutNotApplyingForProbate))

      val form = applyingForProbateForm.fill(ApplicantDetails(isApplyingForProbate = Some(false)))

      implicit val request = createFakeRequestWithReferrerWithBody(referrerURL = referrerURL, host = host, data = form.data.toSeq)

      val result = controller.onEditSubmit(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(iht.controllers.registration.routes.KickoutController.onPageLoad().url))

      val capturedValue = verifyAndReturnStoredRegistationDetails(mockCachingConnector)
      val applicant = capturedValue.applicantDetails.get
      applicant.isApplyingForProbate shouldBe Some(false)

      val storeResult = verifyAndReturnStoredSingleValue(mockCachingConnector)
      storeResult._1 shouldBe RegistrationKickOutHelper.RegistrationKickoutReasonCachingKey
      storeResult._2 shouldBe RegistrationKickOutHelper.KickoutNotApplyingForProbate
    }
  }
}
