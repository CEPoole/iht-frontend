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

package iht.controllers.application.assets.vehicles

import iht.connector.{CachingConnector, IhtConnector}
import iht.connector.IhtConnectors
import iht.controllers.application.ApplicationController
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.basicElements.ShareableBasicEstateElement
import iht.utils.CommonHelper
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object VehiclesOverviewController extends VehiclesOverviewController with IhtConnectors {
  def metrics : Metrics = Metrics
}

trait VehiclesOverviewController extends ApplicationController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIht {
    implicit user => implicit request => {
      val registrationDetails: RegistrationDetails = cachingConnector.getExistingRegistrationDetails

      for {

        applicationDetails: Option[ApplicationDetails]<- ihtConnector.getApplication(
          CommonHelper.getNino(user),
          CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
          registrationDetails.acknowledgmentReference
        )
        vehicles:Option[ShareableBasicEstateElement] = applicationDetails.flatMap(_.allAssets.flatMap(_.vehicles))
      } yield {
        Ok(iht.views.html.application.asset.vehicles.vehicles_overview(vehicles ,registrationDetails))
      }
    }
  }
}
