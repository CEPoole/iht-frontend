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

package iht.models.application.exemptions

import iht.FakeIhtApp
import iht.testhelpers.CommonBuilder
import org.scalatest.mock.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by vineet on 06/11/16.
  */
class CharityTest extends UnitSpec with MockitoSugar with FakeIhtApp{

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  "isComplete" must {

    "return true when Charity is complete" in {
      val charity = CommonBuilder.buildCharity.copy(id = Some("1"),
        name = Some("test"),
        number = Some("121212"),
        totalValue = Some(BigDecimal(1000)))

      charity.isComplete shouldBe true
    }

    "return false when all but one of fields is None" in {
      val charity = CommonBuilder.buildCharity.copy(
        id = Some("1"),
        name = Some("test"),
        number = Some("121212"),
        totalValue = None)

      charity.isComplete shouldBe false
    }

    "return false when all the fields are None" in {
        CommonBuilder.buildCharity.isComplete shouldBe false
    }
  }

  "nameValidationMessage" must {
    "respond correctly for scenario 1 (No charity name, no charity number, charity value)" in {
      val result = CommonBuilder.charity.copy(name = None, number = None)
      result.nameValidationMessage shouldBe Some(messagesApi("site.noCharityNameAndNumberGiven"))
    }
    "respond correctly for scenario 2 (Charity name, no charity number, no charity value)" in {
      val result = CommonBuilder.charity.copy(number = None, totalValue = None)
      result.nameValidationMessage shouldBe None
    }
    "respond correctly for scenario 3 (No charity name, charity number, no charity value)" in {
      val result = CommonBuilder.charity.copy(name = None, totalValue = None)
      result.nameValidationMessage shouldBe Some(messagesApi("site.noCharityNameGiven"))
    }
    "respond correctly for scenario 4 (Charity name, no charity number, charity value)" in {
      val result = CommonBuilder.charity.copy(number = None)
      result.nameValidationMessage shouldBe None
    }
    "respond correctly for scenario 5 (No charity name, charity number, charity value)" in {
      val result = CommonBuilder.charity.copy(name = None)
      result.nameValidationMessage shouldBe Some(messagesApi("site.noCharityNameGiven"))
    }
  }
}
