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

package iht.controllers.registration

import iht.connector.IhtConnectors
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future


object DuplicateRegistrationController extends DuplicateRegistrationController with IhtConnectors

trait DuplicateRegistrationController extends RegistrationController {
  override def guardConditions: Set[Predicate] = Set.empty

  def onPageLoad(ihtReference: String) = authorisedForIht {
    implicit user => implicit request => {
      Future.successful(Ok(iht.views.html.registration.duplicate_registration(ihtReference)))
    }
  }

  def onSubmit = authorisedForIht {
    implicit user => implicit request => {
        Future(Redirect(iht.controllers.estateReports.routes.YourEstateReportsController.onPageLoad()))
    }
  }
}
