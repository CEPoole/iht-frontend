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

package iht.utils

import iht.FakeIhtApp
import iht.constants.Constants
import iht.testhelpers._
import org.scalatest.mock.MockitoSugar
import play.api.mvc.Session
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, ConfidenceLevel, CredentialStrength, IhtAccount}
import uk.gov.hmrc.play.frontend.auth.{AuthContext, LoggedInUser, Principal}
import uk.gov.hmrc.play.test.UnitSpec


class SessionHelperTest extends UnitSpec with FakeIhtApp with MockitoSugar {

  "getNinoFromSession" must {
    "return the nino when it is present in the session" in {
      val nino = "CSXXXXX"
      val request = FakeRequest().withSession(Constants.NINO -> nino)
      SessionHelper.getNinoFromSession(request) shouldBe Some(nino)
    }

    "return the empty string when nino is not present in the session" in {
      val request = FakeRequest().withSession()
      SessionHelper.getNinoFromSession(request) shouldBe empty
    }
  }

  def buildAuthContext(nino:Nino): AuthContext = {
    new AuthContext(
      LoggedInUser(CommonBuilder.firstNameGenerator, None, None, None, CredentialStrength.Strong, ConfidenceLevel.L300 ,""),
      Principal(None, Accounts(iht = Some(IhtAccount("", nino)) )),
      None, None, None, None
    )
  }

  "ensureSessionHasNino" must {
    "throw an exception if the authcontext contains no nino" in {
      val nino = NinoBuilder.randomNino
      a[RuntimeException] shouldBe thrownBy {
        SessionHelper.ensureSessionHasNino(new Session(), CommonBuilder.buildAuthContext())
      }
    }

    "add the nino if it is not present in the session" in {
      val nino = NinoBuilder.randomNino
      val result = SessionHelper.ensureSessionHasNino(new Session(), buildAuthContext(nino))
      result.get(Constants.NINO) shouldBe Some(nino.nino)
    }

    "retrieve the nino if the same nino is present in the session" in {
      val nino = NinoBuilder.randomNino
      val session = new Session() + (Constants.NINO -> nino.name)
      val result = SessionHelper.ensureSessionHasNino(session, buildAuthContext(nino))
      result.get(Constants.NINO) shouldBe Some(nino.nino)
    }

    "replace the nino if a different nino is present in the session" in {
      val nino = NinoBuilder.randomNino
      val newNino = NinoBuilder.randomNino
      val session = new Session() + (Constants.NINO -> nino.name)
      val result = SessionHelper.ensureSessionHasNino(session, buildAuthContext(newNino))
      result.get(Constants.NINO) shouldBe Some(newNino.nino)
    }
  }
}
