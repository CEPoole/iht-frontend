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

package iht.controllers.application.assets.stocksAndShares

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.{MockFormPartialRetriever, CommonBuilder}
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.{Messages, MessagesApi}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import uk.gov.hmrc.play.partials.FormPartialRetriever

/**
 * Created by yasar on 30/06/16.
 */
class StocksAndSharesOverviewControllerTest extends ApplicationControllerTest {

  "StocksAndSharesOverviewControllerTest" must {

    val mockCachingConnector = mock[CachingConnector]
    val mockIhtConnector = mock[IhtConnector]

    def stocksAndSharesOverviewController = new StocksAndSharesOverviewController {
      override val authConnector = createFakeAuthConnector()
      override val cachingConnector = mockCachingConnector
      override val ihtConnector = mockIhtConnector
      override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
    }

    def stocksAndSharesOverviewControllerNotAuthorised = new StocksAndSharesOverviewController {
      override val authConnector = createFakeAuthConnector(false)
      override val cachingConnector = mockCachingConnector
      override val ihtConnector = mockIhtConnector
      override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
    }

    "respond with OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = stocksAndSharesOverviewController.onPageLoad(createFakeRequest())
      status(result) shouldBe (OK)
    }

    "redirect to login page on PageLoad if the user is not logged in" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      createMocksForApplication(mockCachingConnector,
        mockIhtConnector,
        appDetails = Some(applicationDetails),
        getAppDetails = true,
        saveAppDetails= true,
        storeAppDetailsInCache = true)

      val result = stocksAndSharesOverviewControllerNotAuthorised.onPageLoad(createFakeRequest(false))
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    behave like controllerOnPageLoadWithNoExistingRegistrationDetails(mockCachingConnector,
      stocksAndSharesOverviewController.onPageLoad(createFakeRequest()))
  }
  
}
