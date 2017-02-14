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

package iht.views.application.tnrb

import iht.forms.TnrbForms._
import iht.models.application.tnrb.TnrbEligibiltyModel
import iht.testhelpers.CommonBuilder
import iht.utils.tnrb.TnrbHelper
import iht.views.application.YesNoQuestionViewBehaviour
import iht.views.html.application.tnrb.benefit_from_trust
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class BenefitFromTrustViewTest extends YesNoQuestionViewBehaviour[TnrbEligibiltyModel] {
  override def guidanceParagraphs = Set.empty

  def tnrbModel = CommonBuilder.buildTnrbEligibility

  def widowCheck = CommonBuilder.buildWidowedCheck

  override def pageTitle = Messages("iht.estateReport.tnrb.benefitFromTrust.question",
    TnrbHelper.spouseOrCivilPartnerLabel(tnrbModel, widowCheck,
      Messages("page.iht.application.tnrbEligibilty.partner.additional.label.the")))

  override def browserTitle = Messages("page.iht.application.tnrb.benefitFromTrust.browserTitle")

  override def formTarget = Some(iht.controllers.application.tnrb.routes.BenefitFromTrustController.onSubmit())

  override def form: Form[TnrbEligibiltyModel] = benefitFromTrustForm

  override def formToView: Form[TnrbEligibiltyModel] => Appendable =
    form => benefit_from_trust(form, tnrbModel, widowCheck)

  override def cancelComponent = None

  "Applying For Probate View" must {
    behave like yesNoQuestion
  }
}
