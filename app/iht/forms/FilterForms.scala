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

package iht.forms

import play.api.data.Form
import play.api.data.Forms._
import iht.constants.Constants._
import iht.constants.FieldMappings
import iht.utils.IhtFormValidator._

object FilterForms {
  val filterForm = Form(
    mapping(
      filterChoices -> of(radioOptionString("error.selectAnswer", FieldMappings.filterChoicesWithoutHints))
    )
    (
      identity
    )
    (
      (choice) => Some(choice)
    )
  )

  val domicileForm = Form(
    mapping(
      domicile -> of(radioOptionString("error.selectAnswer", FieldMappings.domicileChoices))
    )
    (
      identity
    )
    (
      (choice) => Some(choice)
    )
  )

  val estimateForm = Form(
    mapping(
      estimate -> of(radioOptionString("error.selectAnswer", FieldMappings.estimateChoices))
    )
    (
      identity
    )
    (
      (choice) => Some(choice)
    )
  )
}
