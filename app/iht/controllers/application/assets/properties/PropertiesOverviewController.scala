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

import iht.connector.{CachingConnector, IhtConnector, IhtConnectors}
import iht.controllers.application.ApplicationController
import iht.metrics.Metrics
import iht.models.application.ApplicationDetails
import iht.models.application.assets.{Properties, Property}
import iht.utils.{CommonHelper, StringHelper}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

object PropertiesOverviewController extends PropertiesOverviewController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait PropertiesOverviewController extends ApplicationController {

  def cachingConnector: CachingConnector

  def ihtConnector: IhtConnector

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { registrationDetails =>
          for {
            applicationDetails: Option[ApplicationDetails] <- ihtConnector.getApplication(
              StringHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(registrationDetails.ihtReference),
              registrationDetails.acknowledgmentReference
            )
            propertyList: List[Property] = applicationDetails.map(_.propertyList).getOrElse(Nil)
            properties: Option[Properties] = applicationDetails.flatMap(_.allAssets.flatMap(_.properties))
          } yield {
            Ok(iht.views.html.application.asset.properties.properties_overview(propertyList, properties, registrationDetails))
          }
        }
      }
  }
}
