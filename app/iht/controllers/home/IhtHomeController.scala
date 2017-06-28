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

package iht.controllers.home

import java.util.UUID
import javax.inject.{Inject, Singleton}

import iht.constants.Constants
import iht.controllers.application.ApplicationController
import iht.utils.{CommonHelper, ApplicationStatus => AppStatus}
import iht.viewmodels.application.home.IhtHomeRowViewModel
import play.api.Logger
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.http.{SessionKeys, Upstream4xxResponse}

/**
  *
  * Created by Vineet Tyagi on 18/06/15.
  *
  */

@Singleton
class IhtHomeController @Inject()(
                                   implicit val messagesApi: MessagesApi,
                                   constants:Constants
                                 ) extends ApplicationController {

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        val nino = CommonHelper.getNino(user)

        ihtConnector.getCaseList(nino).map {
          case listOfCases if listOfCases.nonEmpty => {

            listOfCases.foreach { ihtCase =>
              Logger.info("Application status retrieved from DES is ::: " + ihtCase.currentStatus)

              ihtCase.currentStatus match {
                case AppStatus.AwaitingReturn | AppStatus.KickOut => {}
                case _ => {
                  ihtConnector.deleteApplication(nino, ihtCase.ihtRefNo)
                }
              }
            }

            val viewModels = listOfCases.map {
              ihtCase => IhtHomeRowViewModel(nino, ihtCase, ihtConnector)
            }

            Ok(iht.views.html.home.iht_home(viewModels))
              .withSession(request.session + (SessionKeys.sessionId -> s"session-${UUID.randomUUID}") + (constants.NINO -> nino))
          }
        } recover {
          case e: Upstream4xxResponse if e.upstreamResponseCode == 404 =>
            Ok(iht.views.html.home.iht_home(Nil)).withSession(
              CommonHelper.ensureSessionHasNino(request.session, user) +
                (SessionKeys.sessionId -> s"session-${UUID.randomUUID}")
            )
        }
      }
  }
}
