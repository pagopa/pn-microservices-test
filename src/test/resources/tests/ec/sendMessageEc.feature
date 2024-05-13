Feature: Send Digital Message Ec

  @PnEcSendMessage @invioSMS
  Scenario Outline: Invio sms e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId           | channel      |
      | @clientId-delivery | @channel_sms |


  @PnEcSendMessage @invioPEC
  Scenario Outline: Invio pec e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId           | channel      |
      | @clientId-delivery | @channel_pec |

  @PnEcSendMessage @invioEMAIL
  Scenario Outline: Invio email e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId           | channel        |
      | @clientId-delivery | @channel_email |

  @PnEcSendMessage @invioCartaceo
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a paper message
    And waiting for scheduling
    And check if the message has been sent
    Then I send the following paper progress status requests:
      | statusCode | statusDescription                | productType |
      | CON080     | Stampato ed imbustato            | AR          |
      | RECAG004   | Furto/Smarrimanto/deterioramento | AR          |
    And check if paper progress status requests have been accepted
    Examples:
      | clientId       | channel        |
      | @clientId-cons | @channel_paper |

  @PnEcSendMessage @invioCartaceo @sendPaperProgressStatusError
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug ed errore nell'avanzamento dei progressi postalizzazioni
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a paper message
    And waiting for scheduling
    And check if the message has been sent
    Then I send the following paper progress status requests:
      | statusCode     | statusDescription | productType     | iun     | statusDateTime           | clientRequestTimeStamp
      | FakeStatusCode | FakeDescription   | FakeProductType | FakeIun | 2023-07-11T16:15:00.000Z | 2023-07-11T16:15:00.000Z
    And I get "<rc>" result code
    Examples:
      | clientId       | channel        | rc     |
      | @clientId-cons | @channel_paper | 400.02 |


  @PnEcSendMessage @invioPEC @complete_pec
  Scenario Outline: Invio pec e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message
    And check if the message has been sent
    And waiting for scheduling
    Then check if the message has been accepted and has been delivered


    Examples:
      | clientId           | channel      |
      | @clientId-delivery | @channel_pec |


  @PnEcSendMessage @invioEMAIL @complete_mail
  Scenario Outline: invio email e verifica della pubblicszione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId           | channel        |
      | @clientId-delivery | @channel_email |