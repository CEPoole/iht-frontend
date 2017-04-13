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

package iht.controllers

import iht.controllers.auth.CustomPasscodeAuthentication
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.passcode.authentication.{PasscodeAuthenticationProvider, PasscodeVerificationConfig}
import iht.controllers.auth.IhtActions

import scala.concurrent.Future

/**
 * Created by yasar on 7/6/15.
 */
object IhtMainController extends IhtMainController

trait IhtMainController extends FrontendController with CustomPasscodeAuthentication with IhtActions {

  def signOut = customAuthenticatedActionAsync {
    implicit request => {
      Future.successful(Ok(iht.views.html.sign_out()).withNewSession)
    }
  }

  def keepAlive = authorisedForIht {
    implicit user =>
      implicit request => {
        Future.successful(Ok("OK"))
      }

  }
}
