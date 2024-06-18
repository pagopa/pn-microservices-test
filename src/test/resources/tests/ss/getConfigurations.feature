Feature: Get configurations

  @GetConfigurations @getDocumentsConfigs
  Scenario Outline: Reperimento tipologie di documenti e caratteristiche di storage.
    Given "<clientId>" authenticated by "<APIKey>"
    When I get documents configs
    Then I get "<statusCode>" statusCode
    Examples:
      | clientId            | APIKey            | statusCode |
      | @clientId-delivery  | @delivery_api_key | 200        |
      | @clientId-delivery  | INVALID_API_KEY   | 403        |
      | NON_EXISTENT_CLIENT | @delivery_api_key | 403        |

  @GetConfigurations @getCurrentClientConfig
  Scenario Outline: Reperimento dei dettagli di configurazione di un client.
    Given "<clientId>" authenticated by "<APIKey>"
    When I get current client config
    Then I get "<statusCode>" statusCode
    Examples:
      | clientId            | APIKey            | statusCode |
      | @clientId-delivery  | @delivery_api_key | 200        |
      | NON_EXISTENT_CLIENT | @delivery_api_key | 403        |
