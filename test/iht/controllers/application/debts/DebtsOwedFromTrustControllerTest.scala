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

package iht.controllers.application.debts

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.debts.BasicEstateElementLiabilities
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import play.api.test.Helpers._
import iht.constants.Constants._
import iht.constants.IhtProperties._
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.http.HeaderCarrier

class DebtsOwedFromTrustControllerTest extends ApplicationControllerTest {

  implicit val hc = new HeaderCarrier()
  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  def debtsOwedFromTrustController = new DebtsOwedFromATrustController {
    override val cachingConnector = mockCachingConnector
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val ihtConnector = mockIhtConnector

    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC1234567890")
    )

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "DebtsOwedFromTrust" must {
    "return OK on page load" in {
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(CommonBuilder.buildApplicationDetails),
        getAppDetails = true)

      val result = debtsOwedFromTrustController.onPageLoad()(createFakeRequest(isAuthorised = true))

      status(result) should be(OK)
    }

    "save on submit" in {
      val testValue = BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(33)))
      val filledForm = debtsTrustForm.fill(testValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allLiabilities = Some(CommonBuilder
        .buildAllLiabilities.copy(trust = Some(testValue))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = debtsOwedFromTrustController.onSubmit()(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some(routes.DebtsOverviewController.onPageLoad().url + "#" + DebtsOwedFromTrustID))
    }

    "respond with bad request on submit when request is malformed" in {
      val testValue = CommonBuilder.buildBasicEstateElementLiabilities.copy(isOwned = None)
      val filledForm = debtsTrustForm.fill(testValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allLiabilities = Some(CommonBuilder
        .buildAllLiabilities.copy(trust = Some(testValue))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = debtsOwedFromTrustController.onSubmit()(request)
      status(result) should be(BAD_REQUEST)
    }

    "take you to internal server error on failure" in {
      val testValue = BasicEstateElementLiabilities(isOwned = Some(true), value = Some(BigDecimal(33)))
      val filledForm = debtsTrustForm.fill(testValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = None,
        getAppDetails = true,
        saveAppDetails = true)

      val result = debtsOwedFromTrustController.onSubmit()(request)
      status(result) should be(INTERNAL_SERVER_ERROR)
    }

    "save application, wipe out the value and go to Debts overview page on submit when users selects No" in {
      val debtForTrust = BasicEstateElementLiabilities(isOwned = Some(false), value = Some(BigDecimal(33)))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allLiabilities = Some(CommonBuilder
                                              .buildAllLiabilities.copy(trust = Some(debtForTrust))))

      val filledForm = debtsTrustForm.fill(debtForTrust)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails = true)

      val result = debtsOwedFromTrustController.onSubmit()(request)
      status(result) should be(SEE_OTHER)
      redirectLocation(result).get should be(routes.DebtsOverviewController.onPageLoad().url + "#" + DebtsOwedFromTrustID)

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allLiabilities = applicationDetails.allLiabilities.map(_.copy(
        trust = Some(CommonBuilder.buildBasicEstateElementLiabilities.copy(value = None, isOwned = Some(false))))))

      capturedValue shouldBe expectedAppDetails
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      debtsOwedFromTrustController.onPageLoad(createFakeRequest()))
  }
}
