Feature: Send Digital Message Ec


  @PnEcSendMessage @invioSMS
  Scenario Outline: Invio sms e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>"
    Then check if the message has been sent
    Examples:
      | clientId           | channel      | receiver                      |
      | @clientId-delivery | @channel_sms | @sms.receiver.digital.address |


  @PnEcSendMessage @invioPEC @sendPec
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
    And "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message to "<receiver>"
    And check if the message has been sent
    And waiting for scheduling
    Then check if the message has been accepted and has been delivered
    Examples:
      | clientId           | apiKey            | channel      | receiver                      |
      | @clientId-delivery | @delivery_api_key | @channel_pec | @pec.receiver.digital.address |


  @PnEcSendMessage @invioEMAIL @complete_mail
  Scenario Outline: invio email con allegati e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message to "<receiver>"
    Then check if the message has been sent
    Examples:
      | clientId           | apiKey           | channel        | receiver                        |
      | @clientId-delivery | @delivery_api_key | @channel_email | @email.receiver.digital.address |

# --- TEST KO --- #

  @PnEcSendMessage @invioSMS @invioSMS_ko @invioSMS_ko_client_not_authorized
  Scenario Outline: Invio sms con errori di validazione sintattica
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>" with no authorization
    Then i get an error code "<rc>"
    Examples:
      | clientId          | channel      | receiver                     | rc |
      | @clientId-unknown | @channel_sms | @sms.receiver.digital.address |403 |

  @PnEcSendMessage @invioSMS @invioSMS_ko @invioSMS_ko_validazione_sintattica
  Scenario Outline: Invio sms con errori di validazione sintattica
    Given a "<clientId>" and "<channel>" to send on
    When try to send digital message to "<receiver>" with "<requestId>"
    Then i get an error code "<rc>"
    Examples:
      | clientId           | channel      | receiver                      | requestId    | rc |
      | @clientId-delivery | @channel_sms | @sms.receiver.digital.address |   123x       |400 |

  @PnEcSendMessage @invioSMS @invioSMS_ko @invioSMS_ko_duplicate_request
  Scenario Outline: Invio sms di una richiesta gi� effettuata
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>"
    And check if the message has been sent
    When try to send a digital message to "<receiver>" with same requestId
    Then i get an error code "<rc>"
    Examples:
      | clientId           | channel      | receiver                      | rc  |
      | @clientId-delivery | @channel_sms | @sms.receiver.digital.address | 409 |


  @PnEcSendMessage @invioPEC @complete_pec_ko @complete_pec_ko_client_not_authorized
  Scenario Outline: Invio pec con allegati con una pec non valida e verifica della pubblicazione del messaggio di errore nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>" with no authorization
    Then i get an error code "<rc>"
    Examples:
      | clientId          | channel      | receiver              | rc  |
      | @clientId-unknown | @channel_pec | test.test@arubapec.it | 403 |


  @PnEcSendMessage @invioPEC @invioPEC_ko @invioPEC_ko_duplicate_request
  Scenario Outline: Invio pec di una richiesta gi� effettuata
    Given a "<clientId>" and "<channel>" to send on
    And "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message to "<receiver>"
    And check if the message has been sent
    When try to send a digital message to "<receiver>" with same requestId
    Then i get an error code "<rc>"
    Examples:
      | clientId           | apiKey            | channel      | receiver              | rc  |
      | @clientId-delivery | @delivery_api_key | @channel_pec | test.test@arubapec.it | 409 |

  @PnEcSendMessage @invioPEC @invioPEC_ko @invioPEC_ko_validazione_sintattica
  Scenario Outline: Invio pec con errori di validazione sintattica
    Given a "<clientId>" and "<channel>" to send on
    When try to send digital message to "<receiver>" with "<requestId>"
    Then i get an error code "<rc>"
    Examples:
      | clientId           | channel      | receiver                      | requestId    | rc |
      | @clientId-delivery | @channel_pec | @pec.receiver.digital.address |   123x       |400 |

  @PnEcSendMessage @invioPEC @complete_pec_ko
  Scenario Outline: Invio pec con allegati con una pec non valida e verifica della pubblicazione del messaggio di errore nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message to "<receiver>"
    Then check if the message has event code error "<rc>"
    Examples:
      | clientId           | apiKey            | channel      | receiver         | rc   |
     #TODO: Test non valido per mancata gestione della AddressException su develop. La modifica è ancora in hotfix/PN-11261
     #| @clientId-delivery | @delivery_api_key | @channel_pec | .mario.rossi@arubapec.it | C011 |
      | @clientId-delivery | @delivery_api_key | @channel_pec | test.xx@gmail.it | C009 |

  @PnEcSendMessage @invioEMAIL @invioEMAIL_ko @invioEmail_ko_client_not_authorized
  Scenario Outline: Invio email con un client non autorizzato
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>" with no authorization
    Then i get an error code "<rc>"
    Examples:
      | clientId           | channel        | receiver                        | rc |
      | @clientId-unknown  | @channel_email | @email.receiver.digital.address | 403|