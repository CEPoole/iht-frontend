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

package iht.controllers.application.assets.properties

import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets._
import iht.models.application.debts.AllLiabilities
import iht.utils.{CommonHelper, PropertyAndMortgageHelper, StringHelper}
import iht.views.html.application.asset.properties.properties_owned_question
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object PropertiesOwnedQuestionController extends PropertiesOwnedQuestionController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait PropertiesOwnedQuestionController extends EstateController {

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request =>
        estateElementOnPageLoad[Properties](propertiesForm, properties_owned_question.apply, _.allAssets.flatMap(_.properties))
  }

  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>

          val applicationDetailsFuture = ihtConnector.getApplication(StringHelper.getNino(user),
            CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
            regDetails.acknowledgmentReference)

          val boundForm = propertiesForm.bindFromRequest

          applicationDetailsFuture.flatMap {
            case Some(appDetails) =>
              boundForm.fold(
                formWithErrors => {
                  Future.successful(BadRequest(properties_owned_question(formWithErrors, regDetails)))
                },
                propertiesModel => {
                  saveApplication(StringHelper.getNino(user), propertiesModel, appDetails, regDetails)
                }
              )
            case _ => Future.successful(InternalServerError("Application details not found"))
          }
        }
      }
  }

  private def saveApplication(nino: String,
                              properties: Properties,
                              appDetails: ApplicationDetails,
                              regDetails: RegistrationDetails)(implicit request: Request[_],
                                                               hc: HeaderCarrier,
                                                               user: AuthContext): Future[Result] = {


    val updatedAppDetails = appDetails.copy(
      allAssets = Some(appDetails.allAssets.fold(new AllAssets(properties = Some(properties)))(_.copy(
        properties = Some(properties)))),
      propertyList = PropertyAndMortgageHelper.updatePropertyList(properties, appDetails),
      allLiabilities = Some(appDetails.allLiabilities.fold(new AllLiabilities())(_.copy(
        mortgages = PropertyAndMortgageHelper.updateMortgages(properties, appDetails))))
    )

    val adAfterUpdatedForKickout = updateKickout(registrationDetails = regDetails, applicationDetails = updatedAppDetails)
    ihtConnector.saveApplication(nino, adAfterUpdatedForKickout, regDetails.acknowledgmentReference)
      .map { savedApplicationDetails =>
        savedApplicationDetails.fold[Result] {
          Logger.warn("Problem storing Application details. Redirecting to InternalServerError")
          InternalServerError
        } { _ =>
          adAfterUpdatedForKickout.kickoutReason match {
            case Some(_) => Redirect(iht.controllers.application.routes.KickoutController.onPageLoad())
            case _ => PropertyAndMortgageHelper.determineRedirectLocationForPropertiesOwnedQuestion(properties, appDetails)
          }
        }
      }
  }
}
