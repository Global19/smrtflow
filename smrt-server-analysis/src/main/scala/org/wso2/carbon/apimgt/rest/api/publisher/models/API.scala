/**
 * NOTE: This class is auto generated by the akka-scala (beta) swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen
 * For any issue or feedback, please open a ticket via https://github.com/swagger-api/swagger-codegen/issues/new
 */

package org.wso2.carbon.apimgt.rest.api.publisher.models

import org.joda.time.DateTime


case class API (
  /* UUID of the api registry artifact  */
  id: Option[String],
  name: String,
  description: Option[String],
  context: String,
  version: String,
  /* If the provider value is not given user invoking the api will be used as the provider.  */
  provider: Option[String],
  /* Swagger definition of the API which contains details about URI templates and scopes  */
  apiDefinition: String,
  /* WSDL URL if the API is based on a WSDL endpoint  */
  wsdlUri: Option[String],
  status: Option[String],
  responseCaching: Option[String],
  cacheTimeout: Option[Int],
  destinationStatsEnabled: Option[String],
  isDefaultVersion: Boolean,
  /* Supported transports for the API (http and/or https).  */
  transport: Seq[String],
  tags: Option[Seq[String]],
  tiers: Seq[String],
  maxTps: Option[API_maxTps],
  thumbnailUri: Option[String],
  visibility: APIEnums.Visibility,
  visibleRoles: Option[Seq[String]],
  visibleTenants: Option[Seq[String]],
  endpointConfig: String,
  endpointSecurity: Option[API_endpointSecurity],
  /* Comma separated list of gateway environments.  */
  gatewayEnvironments: Option[String],
  sequences: Option[Seq[Sequence]],
  subscriptionAvailability: Option[APIEnums.SubscriptionAvailability],
  subscriptionAvailableTenants: Option[Seq[String]],
  businessInformation: Option[API_businessInformation],
  corsConfiguration: Option[API_corsConfiguration])

object APIEnums {

  type Visibility = Visibility.Value
  type SubscriptionAvailability = SubscriptionAvailability.Value
  
  object Visibility extends Enumeration {
    val PUBLIC = Value("PUBLIC")
    val `PRIVATE` = Value("PRIVATE")
    val RESTRICTED = Value("RESTRICTED")
    val CONTROLLED = Value("CONTROLLED")
  }

  object SubscriptionAvailability extends Enumeration {
    val CurrentTenant = Value("current_tenant")
    val AllTenants = Value("all_tenants")
    val SpecificTenants = Value("specific_tenants")
  }

  
}
