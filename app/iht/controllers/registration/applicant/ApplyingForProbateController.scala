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

package iht.controllers.registration.applicant

import javax.inject.{Inject, Singleton}

import iht.controllers.ControllerHelper.Mode
import iht.forms.registration.ApplicantForms._
import iht.models.{ApplicantDetails, RegistrationDetails}
import iht.utils.{CommonHelper, RegistrationKickOutHelper}
import iht.views.html.registration.{applicant => views}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Request}

@Singleton
class ApplyingForProbateController @Inject()(val messagesApi: MessagesApi) extends RegistrationApplicantControllerWithEditMode {
  def form = applyingForProbateForm

  override def guardConditions: Set[Predicate] = guardConditionsApplicantApplyingForProbateQuestion

  override def getKickoutReason = RegistrationKickOutHelper.checkNotApplyingForProbateKickout

  override val storageFailureMessage = "Store registration details fails on applying for probate submission"

  lazy val submitRoute = routes.ApplyingForProbateController.onSubmit
  lazy val editSubmitRoute = routes.ApplyingForProbateController.onEditSubmit

  def okForPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.applying_for_probate(form, CommonHelper.getDeceasedNameOrDefaultString(name), submitRoute)
    (request, request.acceptLanguages.head, messagesApi.preferred(request)))

  def okForEditPageLoad(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    Ok(views.applying_for_probate(form, CommonHelper.getDeceasedNameOrDefaultString(name), editSubmitRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, messagesApi.preferred(request)))

  def badRequestForSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.applying_for_probate(form, CommonHelper.getDeceasedNameOrDefaultString(name), submitRoute)
    (request, request.acceptLanguages.head, messagesApi.preferred(request)))

  def badRequestForEditSubmit(form: Form[ApplicantDetails], name: Option[String])(implicit request: Request[AnyContent]) =
    BadRequest(views.applying_for_probate(form, CommonHelper.getDeceasedNameOrDefaultString(name), editSubmitRoute, cancelToRegSummary)
    (request, request.acceptLanguages.head, messagesApi.preferred(request)))

  def applyChangesToRegistrationDetails(rd: RegistrationDetails, ad: ApplicantDetails, mode: Mode.Value) = {
    val x = rd.applicantDetails.getOrElse(new ApplicantDetails) copy (
      isApplyingForProbate = ad.isApplyingForProbate)
    rd copy (applicantDetails = Some(x))
  }

  def onwardRoute(rd: RegistrationDetails) = routes.ProbateLocationController.onPageLoad
}
