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

package iht.controllers.application.assets.insurancePolicy

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.MockObjectBuilder._
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder, TestHelper}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

import scala.concurrent.Future

/**
 *
 * Created by Yasar Acar on 18/02/16.
 *
 */
class InsurancePolicyDetailsPayingOtherControllerTest extends ApplicationControllerTest {

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def insurancePolicyDetailsPayingOtherController = new InsurancePolicyDetailsPayingOtherController {
    override val authConnector = createFakeAuthConnector(isAuthorised = true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }
  def insurancePolicyDetailsPayingOtherControllerNotAuthorised = new InsurancePolicyDetailsPayingOtherController {
    override val authConnector = createFakeAuthConnector(isAuthorised = false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  val registrationDetails = CommonBuilder.buildRegistrationDetails copy(
    deceasedDetails = Some(CommonBuilder.buildDeceasedDetails),
    ihtReference = Some("ABC123"))

  lazy val deceasedName = registrationDetails.deceasedDetails.fold("")(x => x.name)

  val insurancePolicyDetails = InsurancePolicy(
    isAnnuitiesBought = Some(false),
    isInsurancePremiumsPayedForSomeoneElse = Some(true),
    value = Some(BigDecimal(7)),
    shareValue = Some(BigDecimal(8)),
    policyInDeceasedName = Some(true),
    isJointlyOwned = Some(true),
    isInTrust = Some(false),
    coveredByExemption = Some(true),
    sevenYearsBefore = Some(true),
    moreThanMaxValue = Some(false)
  )

  val allAssets = CommonBuilder.buildAllAssets copy (insurancePolicy = Some(insurancePolicyDetails))

  val applicationDetails = CommonBuilder.buildApplicationDetails copy (allAssets = Some(allAssets))

  private def createMocks(applicationDetails: ApplicationDetails) = {
    when(mockCachingConnector.getRegistrationDetails(any(), any()))
      .thenReturn(Future.successful(Some(registrationDetails)))
    when(mockIhtConnector.getApplication(any(), any(), any())(any()))
      .thenReturn(Future.successful(Some(applicationDetails)))
    when(mockCachingConnector.storeApplicationDetails(any())(any(), any()))
      .thenReturn(Future.successful(Some(applicationDetails)))
    when(mockIhtConnector.saveApplication(any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(Some(applicationDetails)))
  }

  "InsurancePolicyDetailsPayingOtherController" must {

    "save application and go to Insurance Overview page on submit" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(insurancePolicy = Some(insurancePolicyDetails))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insurancePayingOtherValue = CommonBuilder.buildInsurancePolicy.copy(isInsurancePremiumsPayedForSomeoneElse=Some(true))

      val filledInsuranceForm = insurancePolicyPayingOtherForm.fill(insurancePayingOtherValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = insurancePolicyDetailsPayingOtherController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
    }

    "save application and go to Insurance Overview page on submit where answer as no" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(insurancePolicy = Some(insurancePolicyDetails))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insurancePayingOtherValue = CommonBuilder.buildInsurancePolicy.copy(isInsurancePremiumsPayedForSomeoneElse=Some(false))

      val filledInsuranceForm = insurancePolicyPayingOtherForm.fill(insurancePayingOtherValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = insurancePolicyDetailsPayingOtherController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
    }

    "save application and go to Insurance Overview page on submit where no assets previously saved" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)
      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val insurancePayingOtherValue = CommonBuilder.buildInsurancePolicy.copy(isInsurancePremiumsPayedForSomeoneElse=Some(true))

      val filledInsuranceForm = insurancePolicyPayingOtherForm.fill(insurancePayingOtherValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledInsuranceForm.data.toSeq: _*)

      val result = insurancePolicyDetailsPayingOtherController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
    }

    "respond with bad request when incorrect value are entered on the page" in {

      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = insurancePolicyDetailsPayingOtherController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }

    "redirect to login page onPageLoad if the user is not logged in" in {
      val result = insurancePolicyDetailsPayingOtherControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result).get should be(loginUrl)
    }

    "respond with OK on page load" in {
      createMocks(applicationDetails)
      val result = insurancePolicyDetailsPayingOtherController.onPageLoad(createFakeRequest())
      status(result) should be (OK)
    }

    "redirect to correct page on submit" in {
      createMocks(applicationDetails)

      val filledForm = insurancePolicyPayingOtherForm.fill(insurancePolicyDetails)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledForm.data.toSeq: _*)

      val result = insurancePolicyDetailsPayingOtherController.onSubmit (request)
      redirectLocation(result) should be (Some(iht.controllers.application.assets.insurancePolicy.routes.InsurancePolicyDetailsMoreThanMaxValueController.onPageLoad.url))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      insurancePolicyDetailsPayingOtherController.onPageLoad(createFakeRequest()))
  }
}
