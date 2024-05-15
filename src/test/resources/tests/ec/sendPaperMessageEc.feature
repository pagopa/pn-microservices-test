Feature: Send Paper Message Ec

  @PnEcSendMessage @PAPER @invioCartaceo @testOk
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    And try to send a paper message
    And waiting for scheduling
    Then check if the message has been sent
    Examples:
      | clientId       | channel        |
      | @clientId-cons | @channel_paper |

  @PnEcSendMessage @PAPER @invioCartaceo @complete @testOk
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    And try to send a paper message
    And waiting for scheduling
    And check if the message has been sent
    # statusCode è un campo obbligatorio, gli altri sono opzionali, se non specificati vengono valorizzati con valori di default
    And I send the following paper progress status requests:
      | statusCode |
      | CON080     |
      | RECAG004   |
    Then check if paper progress status requests have been accepted
    Examples:
      | clientId       | channel        |
      | @clientId-cons | @channel_paper |

  @PnEcSendMessage @PAPER @invioCartaceo @testKo
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug ed errore nell'avanzamento dei progressi postalizzazioni
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    Then try to send a paper message
    And waiting for scheduling
    And check if the message has been sent
    # statusCode è un campo obbligatorio, gli altri sono opzionali, se non specificati vengono valorizzati con valori di default
    Then I send the following paper progress status requests:
      | statusCode   | statusDescription   | productType   | iun   | statusDateTime   | clientRequestTimeStamp
      | <statusCode> | <statusDescription> | <productType> | <iun> | <statusDateTime> | <clientRequestTimeStamp>
    And I get "<rc>" result code
    Examples:
      | clientId       | channel        | statusCode | statusDescription | productType | iun | statusDateTime | clientRequestTimeStamp | rc     |
      | @clientId-cons | @channel_paper | FakeStatus |                   |             |     |                |                        | 400.02 |
      | @clientId-cons | @channel_paper | CON080     | desc              |             |     |                |                        | 400.01 |