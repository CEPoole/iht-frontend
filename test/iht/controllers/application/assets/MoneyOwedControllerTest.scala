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

package iht.controllers.application.assets

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

class MoneyOwedControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  var mockIhtConnector = mock[IhtConnector]

  def moneyOwedController = new MoneyOwedController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  def moneyOwedControllerNotAuthorised = new MoneyOwedController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  before {
    mockIhtConnector = mock[IhtConnector]
  }

  "MoneyOwedController" must {

    "redirect to login page on PageLoad if the user is not logged in" in {

      val result = moneyOwedControllerNotAuthorised.onPageLoad(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to login page on Submit if the user is not logged in" in {

      val result = moneyOwedControllerNotAuthorised.onSubmit(createFakeRequest(isAuthorised = false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "respond with OK on page load" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails
      val applicationDetailsTemp = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = moneyOwedController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
    }

    "save application and go to Asset Overview page on submit where yes and value chosen" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(moneyOwed = Some(CommonBuilder.buildBasicElement.copy(value = Some(20), isOwned=Some(true))))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val moneyOwedValue = CommonBuilder.buildBasicElement.copy(value=Some(20), isOwned=Some(true))

      val filledMoneyOwedForm = moneyOwedForm.fill(moneyOwedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledMoneyOwedForm.data.toSeq: _*)

      val result = moneyOwedController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
    }

    "save application and go to Asset Overview page on submit when user selects No" in {

      val moneyOwed = CommonBuilder.buildBasicElement.copy(value = Some(BigDecimal(200)), isOwned=Some(false))

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = Some(CommonBuilder
        .buildAllAssets.copy(moneyOwed = Some(moneyOwed))))

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val filledMoneyOwedForm = moneyOwedForm.fill(moneyOwed)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledMoneyOwedForm.data.toSeq: _*)

      val result = moneyOwedController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)

      val capturedValue = verifyAndReturnSavedApplicationDetails(mockIhtConnector)
      val expectedAppDetails = applicationDetails.copy(allAssets = applicationDetails.allAssets.map(_.copy(
        moneyOwed = Some(CommonBuilder.buildBasicElement.copy(value = None, isOwned = Some(false))))))

      capturedValue shouldBe expectedAppDetails
    }

    "respond with bad request when incorrect value are entered on the page" in {

     implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("value", "utytyyterrrrrrrrrrrrrr"))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = moneyOwedController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
    }

    "save application and go to Asset Overview page on submit where no assets previously saved" in {

      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(allAssets = None)

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val moneyOwedValue = CommonBuilder.buildBasicElement.copy(value=Some(20), isOwned=Some(true))

      val filledMoneyOwedForm = moneyOwedForm.fill(moneyOwedValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledMoneyOwedForm.data.toSeq: _*)

      val result = moneyOwedController.onSubmit (request)
      status(result) shouldBe (SEE_OTHER)
    }

    "respond with bad request and correct error message when no answer is selected" in {
      implicit val fakePostRequest = createFakeRequest().withFormUrlEncodedBody(("", ""))

      createMockToGetRegDetailsFromCacheNoOption(mockCachingConnector)

      val result = moneyOwedController.onSubmit (fakePostRequest)
      status(result) shouldBe (BAD_REQUEST)
      contentAsString(result) should include(messagesApi("error.assets.moneyOwedToDeceased.select",
        CommonBuilder.buildDeceasedDetails.name))
    }


    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      moneyOwedController.onPageLoad(createFakeRequest()))
  }
}
