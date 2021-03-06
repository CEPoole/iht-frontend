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

package iht.controllers.application.assets.insurancePolicy

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.controllers.ControllerHelper
import iht.metrics.Metrics
import iht.utils._
import iht.views.html.application.asset.insurancePolicy.insurance_policy_details_final_guidance
import play.api.mvc.{Call, Request, Result}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object InsurancePolicyDetailsFinalGuidanceController extends InsurancePolicyDetailsFinalGuidanceController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait InsurancePolicyDetailsFinalGuidanceController extends EstateController {

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(registrationDetails)

          for {
            applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference)
          } yield {
            applicationDetails.fold[Result](InternalServerError)(ad =>
              Ok(
                insurance_policy_details_final_guidance(
                  giftsPageRedirect(ad.allGifts.flatMap(_.isGivenAway)),
                  deceasedName
                )
              )
            )
          }
        }
      }
  }

  def giftsPageRedirect(initialGiftsQuestionAnswerOption: Option[Boolean])(implicit request: Request[_]): Call = {
    if (initialGiftsQuestionAnswerOption.fold(false)(identity)) {
      iht.controllers.application.gifts.routes.GiftsOverviewController.onPageLoad()
    } else {
      iht.controllers.application.gifts.routes.GivenAwayController.onPageLoad()
    }
  }
}
