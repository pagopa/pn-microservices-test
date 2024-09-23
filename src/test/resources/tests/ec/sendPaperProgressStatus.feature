Feature: Send Paper Progress Status

  Background:
    Given a "@clientId-delivery-push" and "@channel_paper" to send on
    When "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following attachments:
      | documentType  | fileName                    | mimeType        |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf |
    * try to send a paper message
    * waiting for scheduling
    Then check if the message has been sent

  @PnEcSendMessage @PAPER @complete
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType  | fileName                    | mimeType        | attachmentDocumentType |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf | AR                     |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime |
      | CON080     |                      | @requestId | @now           |
      | RECAG004   |                      | @requestId | @now           |
    Then check if paper progress status requests have been accepted
    Examples:
      | clientId                | apiKey                |
      | @clientId-delivery-push | @apiKey-delivery-push |

  @PnEcSendMessage @PAPER @verificaErroriSemantici
  Scenario Outline: Verifica semantica nell'avanzamento dei progressi di postalizzazione
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When I send the following paper progress status requests:
      | statusCode   | deliveryFailureCause   | iun   | statusDateTime   |
      | <statusCode> | <deliveryFailureCause> | <iun> | <statusDateTime> |
    Then I get "<rc>" result code
    Examples:
      | clientId                | apiKey                | statusCode | deliveryFailureCause | iun        | statusDateTime           | rc     |
      | @clientId-delivery-push | @apiKey-delivery-push | FakeStatus |                      | @requestId | @now                     | 400.02 |
      | @clientId-delivery-push | @apiKey-delivery-push | CON080     | FakeDFC              | @requestId | @now                     | 400.02 |
      | @clientId-delivery-push | @apiKey-delivery-push | CON080     |                      | FakeIun    | @now                     | 400.02 |
      | @clientId-delivery-push | @apiKey-delivery-push | CON080     |                      | @requestId | 2022-07-11T13:02:25.206Z | 400.02 |
      | @clientId-delivery-push | @apiKey-delivery-push | RECRS002A  | M01                  | @requestId | @now                     | 400.02 |
      | @clientId-delivery-push | @apiKey-delivery-push | RECRS002A  | M02                  | @requestId | @now                     | 200.00 |

  @PnEcSendMessage @PAPER @verificaAttachments
  Scenario Outline: Verifica degli allegati nell'avanzamento dei progressi di postalizzazione
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And I prepare the following paper progress status event attachments:
      | attachmentUri   | attachmentDocumentType   |
      | <attachmentUri> | <attachmentDocumentType> |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime |
      | CON080     |                      | @requestId | @now           |
    Then I get "<rc>" result code
    Examples:
      | clientId                | apiKey                | attachmentUri                    | attachmentDocumentType | rc     |
      | @clientId-delivery-push | @apiKey-delivery-push | InvalidUri                       | AR                     | 400.02 |
      | @clientId-delivery-push | @apiKey-delivery-push | safestorage://NonExistentFileKey | AR                     | 400.02 |

  @PnEcSendMessage @PAPER @verificaAttachmentsREC
  Scenario Outline: Verifica dei documentType degli allegati nell'avanzamento degli stati di tipo REC
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And "<clientId>" authenticated by "<apiKey>" uploads the following paper progress status event attachments:
      | documentType  | fileName                    | mimeType        | attachmentDocumentType |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf | NO                     |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime |
      | RECAG010   |                      | @requestId | @now           |
    Then I get "<rc>" result code
    Examples:
      | clientId                | apiKey                | rc     |
      | @clientId-delivery-push | @apiKey-delivery-push | 400.02 |

#TODO: Test non valido per mancata gestione degli eventi duplicati cartacei su develop. La modifica è ancora in feature/PN-11182
#  @PnEcSendMessage @PAPER @verificaDuplicati
#  Scenario Outline: Controllo su eventi duplicati nell'avanzamento dei progressi di postalizzazione
#    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
#    When I send the following paper progress status requests:
#      | statusCode | deliveryFailureCause | iun        | statusDateTime |
#      | RECAG010   |                      | @requestId | @testStartTime |
#    And I send the following paper progress status requests:
#      | statusCode | deliveryFailureCause | iun        | statusDateTime |
#      | RECAG010   |                      | @requestId | @testStartTime |
#    Then I get "<rc>" result code
#    Examples:
#      | clientId                | apiKey                | rc     |
#      | @clientId-delivery-push | @apiKey-delivery-push | 400.02 |