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

package iht.models.des.ihtReturn

import play.api.libs.json.Json

/**
  * Created by vineet on 06/07/17.
  */
case class FreeEstate(estateAssets: Option[Set[Asset]] = None,
                      interestInOtherEstate: Option[InterestInOtherEstate] = None,
                      estateLiabilities: Option[Set[Liability]] = None,
                      estateExemptions: Option[Seq[Exemption]] = None)

object FreeEstate {
  implicit val formats = Json.format[FreeEstate]
}