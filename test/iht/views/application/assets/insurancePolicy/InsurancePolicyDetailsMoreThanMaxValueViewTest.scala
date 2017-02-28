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

package iht.views.application.assets.insurancePolicy

import iht.controllers.application.assets.insurancePolicy.routes
import iht.forms.ApplicationForms._
import iht.models.application.assets.InsurancePolicy
import iht.testhelpers.CommonBuilder
import iht.testhelpers.TestHelper
import iht.views.application.{CancelComponent, YesNoQuestionViewBehaviour}
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_more_than_max_value
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.twirl.api.HtmlFormat.Appendable

class InsurancePolicyDetailsMoreThanMaxValueViewTest extends YesNoQuestionViewBehaviour[InsurancePolicy] {

  lazy val regDetails = CommonBuilder.buildRegistrationDetails1
  lazy val deceasedName = regDetails.deceasedDetails.fold("")(x => x.name)

  override def pageTitle = messagesApi("iht.estateReport.insurancePolicies.overLimitNotOwnEstate.question", deceasedName)

  override def browserTitle = messagesApi("page.iht.application.insurance.policies.section8.browserTitle")

  override def guidance = noGuidance

  override def formTarget = Some(routes.InsurancePolicyDetailsMoreThanMaxValueController.onSubmit())

  override def form: Form[InsurancePolicy] = insurancePolicyMoreThanMaxForm

  override def formToView: Form[InsurancePolicy] => Appendable =
    form => insurance_policy_details_more_than_max_value(form, regDetails)

  override def cancelComponent = Some(CancelComponent(routes.InsurancePolicyOverviewController.onPageLoad(),
    messagesApi("site.link.return.insurance.policies"),
    TestHelper.InsurancePremiumnsYesNoID
  ))

  "InsurancePolicyDetailsMoreThanMaxValueView" must {
    behave like yesNoQuestion
  }
}
