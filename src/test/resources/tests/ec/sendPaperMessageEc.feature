Feature: Send Paper Message Ec

  @PnEcSendMessage @PAPER @invioCartaceo @testOk
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a paper message
    * waiting for scheduling
    Then check if the message has been sent
    Examples:
      | clientId       | channel        | receiver |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address |

  @PnEcSendMessage @PAPER @invioCartaceo @testKo
  Scenario Outline: Invio di un messaggio cartaceo con clientId non valido e verifica dello statusCode
    Given a "<clientId>" and "<channel>" to send on
    When try to send a paper message
    Then I get "<rc>" status code
    Examples:
      | clientId     | channel        | rc  | receiver |
      | FakeClientId | @channel_paper | 403 | @paper.receiver.digital.address |