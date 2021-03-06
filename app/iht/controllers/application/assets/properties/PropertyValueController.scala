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

import iht.connector.{CachingConnector, IhtConnector}
import iht.connector.IhtConnectors
import iht.controllers.application.EstateController
import iht.forms.ApplicationForms._
import iht.metrics.Metrics
import iht.models._
import iht.models.application.ApplicationDetails
import iht.models.application.assets.Property
import iht.utils._
import play.Logger
import play.api.mvc.{Call, Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import iht.constants.IhtProperties._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object PropertyValueController extends PropertyValueController with IhtConnectors {
  def metrics: Metrics = Metrics
}

trait PropertyValueController extends EstateController {

  override val applicationSection = Some(ApplicationKickOutHelper.ApplicationSectionProperties)
  val cancelRedirectLocation = routes.PropertiesOverviewController.onPageLoad()
  val submitUrl = iht.controllers.application.assets.properties.routes.PropertyValueController.onSubmit()
  val cancelUrl = iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onPageLoad()

  def editCancelUrl(id: String) = iht.controllers.application.assets.properties.routes.PropertyDetailsOverviewController.onEditPageLoad(id)

  def editSubmitUrl(id: String) = iht.controllers.application.assets.properties.routes.PropertyValueController.onEditSubmit(id)

  def locationAfterSuccessfulSave(id: String) = CommonHelper.addFragmentIdentifier(
    routes.PropertyDetailsOverviewController.onEditPageLoad(id), Some(AssetsPropertiesPropertyValueID))

  def ihtConnector: IhtConnector

  def cachingConnector: CachingConnector

  def onPageLoad = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

          Future.successful(Ok(iht.views.html.application.asset.properties.property_value(propertyValueForm,
            submitUrl,
            cancelUrl,
            deceasedName)))
        }
      }
  }

  def onEditPageLoad(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        withRegistrationDetails { regDetails =>
          val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)
          for {
            applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(user),
              CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
              regDetails.acknowledgmentReference)
          } yield {
            applicationDetails match {
              case Some(applicationDetails) => {
                applicationDetails.propertyList.find(property => property.id.getOrElse("") equals id).fold {
                  throw new RuntimeException("No Property found for the id")
                } {
                  (matchedProperty) =>
                    Ok(iht.views.html.application.asset.properties.property_value(propertyValueForm.fill(matchedProperty),
                      editSubmitUrl(id),
                      editCancelUrl(id),
                      deceasedName))
                }
              }
              case _ => {
                Logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
                InternalServerError("No Application Details found")
              }
            }
          }
        }
      }
  }


  def onSubmit = authorisedForIht {
    implicit user =>
      implicit request => {
        doSubmit(
          redirectLocationIfErrors = routes.PropertyValueController.onSubmit(),
          submitUrl = submitUrl,
          cancelUrl = cancelUrl)
      }
  }

  def onEditSubmit(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        doSubmit(
          redirectLocationIfErrors = routes.PropertyValueController.onEditSubmit(id),
          submitUrl = editSubmitUrl(id),
          cancelUrl = editCancelUrl(id),
          Some(id))
      }
  }

  private def doSubmit(redirectLocationIfErrors: Call,
                       submitUrl: Call,
                       cancelUrl: Call,
                       propertyId: Option[String] = None)(
                        implicit user: AuthContext, request: Request[_]) = {
    withRegistrationDetails { regDetails =>
      val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)

      val boundForm = propertyValueForm.bindFromRequest
      boundForm.fold(
        formWithErrors => {
          LogHelper.logFormError(formWithErrors)
          Future.successful(BadRequest(iht.views.html.application.asset.properties.property_value(formWithErrors,
            submitUrl,
            cancelUrl,
            deceasedName)))
        },
        property => {
          processSubmit(StringHelper.getNino(user), property, propertyId)
        }
      )
    }
  }

  private def processSubmit(nino: String,
                            property: Property,
                            propertyId: Option[String] = None)(
                             implicit request: Request[_], hc: HeaderCarrier, user: AuthContext): Future[Result] = {

    withRegistrationDetails { registrationData =>
      val ihtReference = CommonHelper.getOrExceptionNoIHTRef(registrationData.ihtReference)
      val applicationDetailsFuture: Future[Option[ApplicationDetails]] =
        ihtConnector.getApplication(nino, ihtReference, registrationData.acknowledgmentReference)

      applicationDetailsFuture.flatMap { optionApplicationDetails =>
        val tuplePropertiesAndID: (List[Property], String) = addPropertyToPropertyList(property.copy(id = propertyId),
          optionApplicationDetails.fold[List[Property]](Nil)(ad => ad.propertyList))

        val updatedProperties: List[Property] = tuplePropertiesAndID._1

        val propertyID: String = tuplePropertiesAndID._2

        val ad = updateKickout(registrationDetails = registrationData,
          applicationDetails = optionApplicationDetails.map(ad => ad.copy(propertyList = updatedProperties)).fold
          (ApplicationDetails(propertyList = updatedProperties))(identity),
          applicationID = Some(propertyID))

        ihtConnector.saveApplication(nino, ad, registrationData.acknowledgmentReference) map {
          case Some(_) => {
            Redirect(ad.kickoutReason.fold(locationAfterSuccessfulSave(propertyID)) {
              _ => {
                cachingConnector.storeSingleValue(ApplicationKickOutHelper.applicationLastSectionKey, applicationSection.fold("")(identity))
                cachingConnector.storeSingleValue(ApplicationKickOutHelper.applicationLastIDKey, propertyID)
                kickoutRedirectLocation
              }
            })
          }
          case _ => {
            Logger.warn("Problem saving Application details. Redirecting to InternalServerError")
            InternalServerError
          }
        }
      }
    }
  }

  def onEditPageLoadForKickout(id: String) = authorisedForIht {
    implicit user =>
      implicit request => {
        doEditPageLoad(id, Some(kickoutRedirectLocation))
      }
  }

  private def doEditPageLoad(id: String, cancelUrl: Option[Call])(implicit request: Request[_], user: AuthContext) = {
    withRegistrationDetails { regDetails =>
      val deceasedName = DeceasedInfoHelper.getDeceasedNameOrDefaultString(regDetails)
      for {
        applicationDetails <- ihtConnector.getApplication(StringHelper.getNino(user),
          CommonHelper.getOrExceptionNoIHTRef(regDetails.ihtReference),
          regDetails.acknowledgmentReference)
      } yield {
        applicationDetails match {
          case Some(applicationDetails) => {
            applicationDetails.propertyList.find(property => property.id.getOrElse("") equals id).fold {
              Redirect(routes.PropertyValueController.onEditPageLoad(id))
            } {
              (matchedProperty) =>
                Ok(iht.views.html.application.asset.properties.property_value(propertyValueForm.fill(matchedProperty),
                  routes.PropertyValueController.onEditSubmit(id),
                  editCancelUrl(id),
                  deceasedName
                ))
            }
          }
          case _ => {
            Logger.warn("Problem retrieving Application Details. Redirecting to Internal Server Error")
            InternalServerError("No Application Details found")
          }
        }
      }
    }
  }

  def addPropertyToPropertyList(property: Property, propertyList: List[Property]): (List[Property], String) = {

    val seekID = property.id.getOrElse("")
    propertyList.find(x => x.id.getOrElse("") equals seekID) match {
      case None => {
        val nextID = nextId(propertyList)
        val updatedList = propertyList :+ property.copy(id = Some(nextID))
        (updatedList, nextID)
      }
      case Some(matchedProperty) => {
        val updatedProperty = matchedProperty.copy(id = property.id, value = property.value)
        val updatedList: List[Property] = propertyList.updated(propertyList.indexOf(matchedProperty), updatedProperty)
        (updatedList, seekID)
      }
    }
  }
}
