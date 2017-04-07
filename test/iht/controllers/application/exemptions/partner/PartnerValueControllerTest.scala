/*
 * Copyright 2017 HM Revenue & Customs
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

package iht.controllers.application.exemptions.partner

import iht.connector.{CachingConnector, IhtConnector}
import iht.controllers.application.ApplicationControllerTest
import iht.forms.ApplicationForms._
import iht.models.application.exemptions.PartnerExemption
import iht.testhelpers.CommonBuilder
import iht.testhelpers.MockObjectBuilder._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.Helpers._
import iht.models.application.ApplicationDetails
import iht.testhelpers.TestHelper._
import iht.utils.CommonHelper._

/**
 * Created by jennygj on 03/08/16.
 */
class PartnerValueControllerTest extends ApplicationControllerTest{

  val mockCachingConnector = mock[CachingConnector]
  val mockIhtConnector = mock[IhtConnector]

  def setUpTests(applicationDetails: ApplicationDetails) = {
    createMocksForApplication(mockCachingConnector,
      mockIhtConnector,
      appDetails = Some(applicationDetails),
      getAppDetails = true,
      saveAppDetails= true,
      storeAppDetailsInCache = true)
  }

  def partnerValueController = new PartnerValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised=true)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  def partnerValueControllerNotAuthorised = new PartnerValueController {
    override val authConnector = createFakeAuthConnector(isAuthorised=false)
    override val cachingConnector = mockCachingConnector
    override val ihtConnector = mockIhtConnector
  }

  "PartnerValueControllerTest" must {


    "redirect to log in page if user is not logged in on page load" in {
      val result = partnerValueControllerNotAuthorised.onPageLoad(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "redirect to log in page if user is not logged in on submit" in {
      val result = partnerValueControllerNotAuthorised.onSubmit(createFakeRequest())
      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be (Some(loginUrl))
    }

    "return an OK on page load" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      setUpTests(applicationDetails)

      val result = partnerValueController.onPageLoad (createFakeRequest())
      status(result) shouldBe (OK)
      contentAsString(result) should include (messagesApi("iht.estateReport.exemptions.partner.returnToAssetsLeftToSpouse"))
      contentAsString(result) should include (messagesApi("iht.saveAndContinue"))
    }

    "save and return to parent page if no value is entered and page is submitted" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          None, Some(true), None, None, None, None, None)))))

      val filledPartnerValueForm = partnerValueForm.fill(CommonBuilder.buildPartnerExemption.
        copy(totalAssets = None))

      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPartnerValueForm.data.toSeq: _*)

      setUpTests(applicationDetails)

      val result = partnerValueController.onSubmit()(request)
      status(result) should be (BAD_REQUEST)
    }

    "display the correct title on page" in {
      val result = partnerValueController.onPageLoad (createFakeRequest())
      status(result) shouldBe OK
      contentAsString(result) should include (messagesApi("page.iht.application.exemptions.partner.totalAssets.label"))
    }

    "redirect to overview page when save and continue is clicked" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails.copy(
        allExemptions = Some(CommonBuilder.buildAllExemptions.copy(partner = Some(PartnerExemption(
          Some(true), Some(true), None, None, None, None, None)))))

      setUpTests(applicationDetails)

      val partnerValue = CommonBuilder.buildPartnerExemption.copy(isAssetForDeceasedPartner = Some(true))

      val filledPartnerValueForm = partnerValueForm.fill(partnerValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPartnerValueForm.data
        .toSeq: _*)

      val result = partnerValueController.onSubmit(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(addFragmentIdentifierToUrl(routes.PartnerOverviewController.onPageLoad().url, ExemptionsPartnerValueID)))
    }

    "redirect to overview page on submit when there is no exemptions present" in {
      val applicationDetails = CommonBuilder.buildApplicationDetails

      setUpTests(applicationDetails)

      val partnerValue = CommonBuilder.buildPartnerExemption.copy(totalAssets = Some(BigDecimal(1000)))
      val filledPartnerValueForm = partnerValueForm.fill(partnerValue)
      implicit val request = createFakeRequest().withFormUrlEncodedBody(filledPartnerValueForm.data
        .toSeq: _*)

      val result = partnerValueController.onSubmit(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) should be(Some(addFragmentIdentifierToUrl(routes.PartnerOverviewController.onPageLoad().url, ExemptionsPartnerValueID)))
    }
  }

}
