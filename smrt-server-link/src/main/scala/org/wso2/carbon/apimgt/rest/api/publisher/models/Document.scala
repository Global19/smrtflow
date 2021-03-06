/**
  * NOTE: This class is auto generated by the akka-scala (beta) swagger code generator program.
  * https://github.com/swagger-api/swagger-codegen
  * For any issue or feedback, please open a ticket via https://github.com/swagger-api/swagger-codegen/issues/new
  */
package org.wso2.carbon.apimgt.rest.api.publisher.models

import org.joda.time.DateTime

case class Document(documentId: Option[String],
                    name: String,
                    `type`: DocumentEnums.`Type`,
                    summary: Option[String],
                    sourceType: DocumentEnums.SourceType,
                    sourceUrl: Option[String],
                    otherTypeName: Option[String],
                    visibility: DocumentEnums.Visibility)

object DocumentEnums {

  type `Type` = `Type`.Value
  type SourceType = SourceType.Value
  type Visibility = Visibility.Value

  object `Type` extends Enumeration {
    val HOWTO = Value("HOWTO")
    val SAMPLES = Value("SAMPLES")
    val PUBLICFORUM = Value("PUBLIC_FORUM")
    val SUPPORTFORUM = Value("SUPPORT_FORUM")
    val APIMESSAGEFORMAT = Value("API_MESSAGE_FORMAT")
    val SWAGGERDOC = Value("SWAGGER_DOC")
    val OTHER = Value("OTHER")
  }

  object SourceType extends Enumeration {
    val INLINE = Value("INLINE")
    val URL = Value("URL")
    val FILE = Value("FILE")
  }

  object Visibility extends Enumeration {
    val OWNERONLY = Value("OWNER_ONLY")
    val `PRIVATE` = Value("PRIVATE")
    val APILEVEL = Value("API_LEVEL")
  }

}
