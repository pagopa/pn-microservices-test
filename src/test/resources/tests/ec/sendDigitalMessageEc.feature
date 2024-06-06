Feature: Send Digital Message Ec

  @PnEcSendMessage @getClientConfig
    Scenario Outline: Recupero di un elemento dal database tramite chiamata GET all'endpoint
    Given a "<clientId>" to send request
    When try to get client configurations
    Then i get response "<rc>"
    Examples:
      | clientId | rc |
      | @clientId-delivery | 200 |

  @PnEcSendMessage @getAllClients
  Scenario Outline: Configurazione del client tramite GET all'endpoint
    Given a "<clientId>" to send request
    When try to get all client configurations
    Then i get response "<rc>"
    Examples:
      | clientId | rc |
      | @clientId-delivery | 200 |

  @PnEcSendMessage @invioSMS
  Scenario Outline: Invio sms e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>"
    Then check if the message has been sent
    Examples:
      | clientId           | channel      | receiver                      |
      | @clientId-delivery | @channel_sms | @sms.receiver.digital.address |


  @PnEcSendMessage @invioPEC @pec_ok
  Scenario Outline: Invio pec e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>"
    Then check if the message has been sent
    Examples:
      | clientId           | channel      | receiver                      |
      | @clientId-delivery | @channel_pec | @pec.receiver.digital.address |

  @PnEcSendMessage @invioEMAIL
  Scenario Outline: Invio email e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>"
    Then check if the message has been sent
    Examples:
      | clientId           | channel        | receiver                        |
      | @clientId-delivery | @channel_email | @email.receiver.digital.address |

  @PnEcSendMessage @invioPEC @complete_pec
  Scenario Outline: Invio pec con allegati e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message to "<receiver>"
    And check if the message has been sent
    And waiting for scheduling
    Then check if the message has been accepted and has been delivered
    Examples:
      | clientId           | channel      | receiver                      |
      | @clientId-delivery | @channel_pec | @pec.receiver.digital.address |


  @PnEcSendMessage @invioEMAIL @complete_mail
  Scenario Outline: invio email con allegati e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message to "<receiver>"
    Then check if the message has been sent
    Examples:
      | clientId           | channel        | receiver                        |
      | @clientId-delivery | @channel_email | @email.receiver.digital.address |

# --- TEST KO --- #

  @PnEcSendMessage @getClientConfig_ko
  Scenario Outline: Configurazione del client tramite GET all'endpoint
    Given a "<clientId>" to send request
    When try to get client configurations
    Then i get response "<rc>"
    Examples:
      | clientId | rc |
      | abc102827 | 404 |

  @PnEcSendMessage @invioPEC @complete_pec_ko
  Scenario Outline: Invio pec con allegati con una email non valida e verifica della pubblicazione del messaggio di errore nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message to "<receiver>"
    Then check if the message has event code error "<rc>"
    Examples:
      | clientId           | channel      | receiver      | rc |
      | @clientId-delivery | @channel_pec | .mario.ottone@arubapec.it | C011 |

