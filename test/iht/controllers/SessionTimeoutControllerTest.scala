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

package iht.controllers

import iht.connector.{CachingConnector, ExplicitAuditConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.testhelpers.MockFormPartialRetriever
import play.api.http.Status._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

class SessionTimeoutControllerTest extends ApplicationControllerTest with UnitSpec {
  implicit val hc = new HeaderCarrier()
  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]
  val mockAuditConnector = mock[ExplicitAuditConnector]

  def controller = new SessionTimeoutController{
    override implicit val formPartialRetriever: FormPartialRetriever = MockFormPartialRetriever
  }

  implicit val request = FakeRequest()

  "onApplicationPageLoad method" must {
    "execute successfully in" in {
      val result = controller.onRegistrationPageLoad()(request)
      status(result) shouldBe OK
    }
  }

  "onRegistrationPageLoad method" must {
    "execute successfully in" in {
      val result = controller.onApplicationPageLoad()(request)
      status(result) shouldBe OK
    }
  }
}
