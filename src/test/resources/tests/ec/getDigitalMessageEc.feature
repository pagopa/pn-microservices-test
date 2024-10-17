Feature: Get Digital Message Ec

  @PnEcGetMessage @getClient @getClientConfig
  Scenario Outline: Recupero di un elemento dal database tramite chiamata GET all'endpoint
    Given a "<clientId>" to send request
    When try to get client configurations
    Then i get response "<rc>"
    Examples:
      | clientId           | rc  |
      | @clientId-delivery | 200 |

  @PnEcGetMessage @getClient @getAllClients
  Scenario Outline: Configurazione del client tramite GET all'endpoint
    Given a "<clientId>" to send request
    When try to get all client configurations
    Then i get response "<rc>"
    Examples:
      | clientId           | rc  |
      | @clientId-delivery | 200 |
    
 @PnEcGetMessage @getRequest @getRequestId
   Scenario Outline: Get di una richieta presente a sistema
      Given a "<clientId>" to send request
      When try to get request by "<requestId>"
      Then i get response "<rc>"
      Examples:
        | clientId           | requestId  | rc |
        | @clientId-delivery | @requestId |200 |

  @PnEcGetMessage @getRequest @getRequestMessageId
  Scenario Outline: Get di una richieta presente a sistema tramite messageID
    Given a "<clientId>" to send request
    When try to get request by messageId "<messageId>"
    Then i get response "<rc>"
    Examples:
      | clientId           | messageId  | rc |
      | @clientId-delivery | @messageId | 200 |

    @PnEcGetMessage @getPec @getPec_ok
  Scenario Outline: Get di una PEC tramite requestId
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>"
    And check if the message has been sent
    When try to get result
    Then i get response "<rc>"
    Examples:
      | clientId           | channel      | receiver                      | rc  |
      | @clientId-delivery | @channel_pec | @pec.receiver.digital.address | 200 |

  @PnEcGetMessage @getEmail @getEmail_ok
  Scenario Outline: Get di una email tramite requestId
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>"
    And check if the message has been sent
    When try to get result
    Then i get response "<rc>"
    Examples:
      | clientId           | channel        | receiver                        | rc  |
      | @clientId-delivery | @channel_email | @email.receiver.digital.address | 200 |

  @PnEcGetMessage @getSms @getSms_ok
  Scenario Outline: Get di un SMS tramite requestId
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>"
    And check if the message has been sent
    When try to get result
    Then i get response "<rc>"
    Examples:
      | clientId           | channel      | receiver                      | rc  |
      | @clientId-delivery | @channel_sms | @sms.receiver.digital.address | 200 |

  @PnEcGetMessage @getPaper @getPaper_ok
  Scenario Outline: Get di un cartaceo tramite requestId
    Given a "<ecClientId>" and "<channel>" to send on
    When "<ssClientId>" authenticated by "<ssApiKey>" uploads the following attachments:
      | documentType  | fileName                    | mimeType        |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf |
    When try to send a paper message to "<receiver>"
    * waiting for scheduling
    And check if the message has been sent
    And try to get result
    Then i get response "<rc>"
    Examples:
      | ecClientId     | ssClientId              | ssApiKey              | channel        | receiver                        | rc  |
      | @clientId-cons | @clientId-delivery-push | @apiKey-delivery-push | @channel_paper | @paper.receiver.digital.address | 200 |

  @PnEcGetMessage @getAttachments @getAttachment_ok
  Scenario Outline: Get di un allegato tramite fileKey
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When try to get attachment with a "<fileKey>"
    Then i get response "<rc>"
    Examples:
      | clientId       | apiKey       | fileKey  | rc  |
      | @clientId-cons | @apiKey-cons | @fileKey | 200 |
    
    # --- TEST KO --- #

  @PnEcGetMessage @getClient @getClientConfig_ko
  Scenario Outline: Configurazione del client tramite GET all'endpoint
    Given a "<clientId>" to send request
    When try to get client configurations
    Then i get response "<rc>"
    Examples:
      | clientId  | rc  |
      | abc102827 | 404 |

  @PnEcGetMessage @getRequest @getRequest_ko
  Scenario Outline: Get di una richieta presente a sistema
    Given a "<clientId>" to send request
    When try to get request by "<requestId>"
    Then i get response "<rc>"
    Examples:
      | clientId           | requestId        | rc  |
      | @clientId-delivery |  notFoundRequest | 404 |

  @PnEcGetMessage @getRequest @getRequestMessageId_ko
  Scenario Outline: Get di una richieta presente a sistema tramite messageID
    Given a "<clientId>" to send request
    When try to get request by messageId "<messageId>"
    Then i get response "<rc>"
    Examples:
      | clientId           | messageId         | rc  |
      | @clientId-delivery |                   | 400 |
      | @clientId-delivery | messageIdNotFound | 404 |

    @PnEcGetMessage @getPec @getPec_ko
      Scenario Outline: Get di una PEC con un requestId non presente a sistema
      Given a "<clientId>" and "<channel>" to send on
      When try to get result with a "<requestId>"
      Then i get response "<rc>"
      Examples:
        | clientId           | channel      | requestId       | rc  |
        | @clientId-delivery | @channel_pec | notFoundRequest | 404 |

  @PnEcGetMessage @getEmail @getEmail_ko
  Scenario Outline: Get di una email con un requestId non presente a sistema
    Given a "<clientId>" and "<channel>" to send on
    When try to get result with a "<requestId>"
    Then i get response "<rc>"
    Examples:
      | clientId           | channel        | requestId         | rc  |
      | @clientId-delivery | @channel_email | notFoundRequest   | 404 |

  @PnEcGetMessage @getSms @getSms_ko
  Scenario Outline: Get di un SMS con un requestId non presente a sistema
    Given a "<clientId>" and "<channel>" to send on
    When try to get result with a "<requestId>"
    Then i get response "<rc>"
    Examples:
      | clientId           | channel      | requestId         | rc  |
      | @clientId-delivery | @channel_sms | notFoundRequest   | 404 |

  @PnEcGetMessage @getPaper @getPaper_ko
  Scenario Outline: Get di un cartaceo con un requestId non presente a sistema
    Given a "<clientId>" and "<channel>" to send on
    When try to get result with a "<requestId>"
    Then i get response "<rc>"
    Examples:
      | clientId       | channel        | requestId         | rc  |
      | @clientId-cons | @channel_paper | notFoundRequest   | 404 |

  @PnEcGetMessage @getAttachments @getAttachment_ko
  Scenario Outline: Get di un allegato tramite fileKey
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When try to get attachment with a "<fileKey>"
    Then i get response "<rc>"
    Examples:
      | clientId           | apiKey       | fileKey  | rc  |
      | @clientId-cons     | @apiKey-cons | aaa.pdf  | 404 |