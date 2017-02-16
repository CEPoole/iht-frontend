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

package iht.views.application.assets.properties

import iht.forms.ApplicationForms._
import iht.models.application.assets.Property
import iht.testhelpers.CommonBuilder
import iht.views.application.{CancelComponent, ValueViewBehaviour}
import iht.views.html.application.asset.properties.{delete_property_confirm, property_value}
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat.Appendable

class DeletePropertyConfirmViewTest extends ValueViewBehaviour[Property] {
  def registrationDetails = CommonBuilder.buildRegistrationDetails1

  def deceasedName = registrationDetails.deceasedDetails.map(_.name).fold("")(identity)

  override def guidanceParagraphs = Set(
    Messages("page.iht.application.assets.main-section.properties.delete.warning")
  )

  override def pageTitle = Messages("page.iht.application.propertyDetails.deleteProperty.title")

  override def browserTitle = Messages("page.iht.application.propertyDetails.deleteProperty.title")

  override def formTarget = Some(CommonBuilder.DefaultCall1)

  override val cancelId: String = "cancel-button"

  override def cancelComponent = Some(
    CancelComponent(
      CommonBuilder.DefaultCall2,
      Messages("iht.estateReport.assets.properties.returnToAddAProperty")
    )
  )

  override def form: Form[Property] = propertyValueForm

  override def formToView: Form[Property] => Appendable =
    form =>
      delete_property_confirm(form)

  "Permanent home page Question View" must {
    behave like valueView()
  }
}
