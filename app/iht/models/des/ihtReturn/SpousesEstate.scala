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

package iht.models.des.ihtReturn

import play.api.libs.json.Json

/**
  * Created by vineet on 06/07/17.
  */
case class SpousesEstate(domiciledInUk: Option[Boolean]= None,
                         whollyExempt: Option[Boolean]= None,
                         jointAssetsPassingToOther: Option[Boolean]= None,
                         otherGifts: Option[Boolean]=None,
                         agriculturalOrBusinessRelief: Option[Boolean]= None,
                         giftsWithReservation: Option[Boolean]= None,
                         benefitFromTrust: Option[Boolean]= None,
                         unusedNilRateBand: Option[BigDecimal]= None)

object SpousesEstate {
  implicit val formats = Json.format[SpousesEstate]
}
