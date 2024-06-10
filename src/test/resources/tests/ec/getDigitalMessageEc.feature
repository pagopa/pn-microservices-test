Feature: Get Digital Message Ec

  @PnEcGetMessage @getClient @getClientConfig
  Scenario Outline: Recupero di un elemento dal database tramite chiamata GET all'endpoint
    Given a "<clientId>" to send request
    When try to get client configurations
    Then i get response "<rc>"
    Examples:
      | clientId | rc |
      | @clientId-delivery | 200 |

  @PnEcGetMessage @getClient @getAllClients
  Scenario Outline: Configurazione del client tramite GET all'endpoint
    Given a "<clientId>" to send request
    When try to get all client configurations
    Then i get response "<rc>"
    Examples:
      | clientId | rc |
      | @clientId-delivery | 200 |


  @PnEcGetMessage @getPec
  Scenario Outline: GET di una PEC tramite requestId
    Given a "<clientId>" to send request
    When try to get pec result
    Then i get response "<rc>"
    Examples:
      | clientId | rc |
      | @clientId-delivery | 200 |
    
    # --- TEST KO --- #

  @PnEcGetMessage @getClient @getClientConfig_ko
  Scenario Outline: Configurazione del client tramite GET all'endpoint
    Given a "<clientId>" to send request
    When try to get client configurations
    Then i get response "<rc>"
    Examples:
      | clientId | rc |
      | abc102827 | 404 |