Feature: Send Paper Progress Status

  Background:
    Given a "@clientId-cons" and "@channel_paper" to send on
    When "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following attachments:
      | documentType  | fileName                    | mimeType        |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf |
    * try to send a paper message
    * waiting for scheduling
    Then check if the message has been sent

  @PnEcSendMessage @PAPER @complete
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following attachments:
      | documentType  | fileName                    | mimeType        | attachmentDocumentType |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf | AR                     |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | courier  |
      | CON080     |                      | @requestId | @now           |          |
      | RECAG004   |                      | @requestId | @now           | YXYXYXYX |
    Then check if paper progress status requests have been accepted
    Examples:
      | clientId       | apiKey       |
      | @clientId-cons | @apiKey-cons |

  @PnEcSendMessage @PAPER @verificaErroriSemantici @verificaErrori
  Scenario Outline: V2-Verifica semantica nell'avanzamento dei progressi di postalizzazione
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When I send the following paper progress status requests:
      | statusCode   | deliveryFailureCause   | iun   | statusDateTime   | clientRequestTimestamp   |
      | <statusCode> | <deliveryFailureCause> | <iun> | <statusDateTime> | <clientRequestTimestamp> |
    Then I get "<rc>" result code
    Examples:
      | clientId       | apiKey       | statusCode | deliveryFailureCause | iun        | statusDateTime           | clientRequestTimestamp   | rc     |
      # Verifica consistenza dati
      | @clientId-cons | @apiKey-cons | CON080     |                      |            | @now                     | @now                     | 200.00 |
      | @clientId-cons | @apiKey-cons | RECRS002A  | M02                  | @requestId | @now                     | @now                     | 200.00 |

  @PnEcSendMessage @PAPER @verificaErroriSemantici @verificaErrori
  Scenario Outline: Verifica semantica nell'avanzamento dei progressi di postalizzazione
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When I send the following paper progress status requests:
    | statusCode   | deliveryFailureCause   | iun   | statusDateTime   | clientRequestTimestamp   |
    | <statusCode> | <deliveryFailureCause> | <iun> | <statusDateTime> | <clientRequestTimestamp> |
    And I get "<rc>" result code
    Then I verify the record in pn-EcScartiConsolidatore
    Examples:
      | clientId       | apiKey       | statusCode | deliveryFailureCause | iun        | statusDateTime           | clientRequestTimestamp   | rc     |
      # Verifica consistenza dati
      | @clientId-cons | @apiKey-cons | CON080     |                      | FakeIun    | @now                     | @now                     | 400.02 |
      | @clientId-cons | @apiKey-cons | FakeStatus |                      | @requestId | @now                     | @now                     | 400.02 |
      | @clientId-cons | @apiKey-cons | CON080     | FakeDFC              | @requestId | @now                     | @now                     | 400.02 |
      | @clientId-cons | @apiKey-cons | RECRS002A  | M01                  | @requestId | @now                     | @now                     | 400.02 |
      # Verifiche temporali
      | @clientId-cons | @apiKey-cons | CON080     |                      | @requestId | 2022-07-11T13:02:25.206Z | @now                     | 400.02 |
      | @clientId-cons | @apiKey-cons | CON080     |                      | @requestId | 2100-07-11T13:02:25.206Z | @now                     | 400.02 |
      | @clientId-cons | @apiKey-cons | CON080     |                      | @requestId | @now                     | 2100-07-11T13:02:25.206Z | 400.02 |

  @PnEcSendMessage @PAPER @verificaAttachments @verificaErrori
  Scenario Outline: Verifica degli allegati nell'avanzamento dei progressi di postalizzazione
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And I prepare the following paper progress status event attachments:
      | attachmentUri   | attachmentDocumentType   |
      | <attachmentUri> | <attachmentDocumentType> |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime |
      | CON080     |                      | @requestId | @now           |
    And I get "<rc>" result code
    Then I verify the record in pn-EcScartiConsolidatore
    Examples:
      | clientId       | apiKey       | attachmentUri                    | attachmentDocumentType | rc     |
      | @clientId-cons | @apiKey-cons | InvalidUri                       | AR                     | 400.02 |
      | @clientId-cons | @apiKey-cons | safestorage://NonExistentFileKey | AR                     | 400.02 |

  @PnEcSendMessage @PAPER @verificaAttachmentsREC @verificaErrori
  Scenario Outline: Verifica dei documentType degli allegati nell'avanzamento degli stati di tipo REC
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following paper progress status event attachments:
      | documentType  | fileName                    | mimeType        | attachmentDocumentType |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf | NO                     |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime |
      | RECAG010   |                      | @requestId | @now           |
    And I get "<rc>" result code
    Then I verify the record in pn-EcScartiConsolidatore
    Examples:
      | clientId       | apiKey       | rc     |
      | @clientId-cons | @apiKey-cons | 400.02 |

  @PnEcSendMessage @PAPER @verificaDuplicati
  Scenario Outline: Controllo su eventi duplicati nell'avanzamento dei progressi di postalizzazione.
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following paper progress status event attachments:
      | documentType  | fileName                    | mimeType        | attachmentDocumentType |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf | AR                     |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType   | courier     |
      | RECAG010   |                      | @requestId | @testStartTime | <productType> | <courier1>  |
    And I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType   | courier     |
      | RECAG010   |                      | @requestId | @testStartTime | <productType> | <courier2>  |
    Then I get "<rc>" result code
    Examples:
      | clientId       | apiKey       | productType                          | courier1        | courier2        | rc     |
      # Il productType è presente nella configurazione di ExternalChannel PnEcDuplicatesCheck
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check    | @paper.courier1 | @paper.courier2 | 400.02 |
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check    |                 | @paper.courier2 | 400.02 |
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check    | @paper.courier1 | @paper.courier1 | 400.02 |
      # Il productType non è presente in PnEcDuplicatesCheck
      | @clientId-cons | @apiKey-cons | @productType_not_for_duplicates_check| @paper.courier1| @paper.courier2 | 200.00 |

    #per gli allegati multipli è stato fatto un test puntuale con una chiamata postman
  @PnEcSendMessage @PAPER @verificaDuplicati @MultipleAttachments
  Scenario Outline: Controllo su eventi duplicati nell'avanzamento dei progressi di postalizzazione
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following paper progress status event attachments:
      | documentType  | fileName                    | mimeType        | attachmentDocumentType |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf | AR                     |
      | @doc_type_aar | src/test/resources/test_pdf.pdf | application/pdf | AR                     |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType   |
      | RECAG010   |                      | @requestId | @testStartTime | <productType> |
    And I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType   |
      | RECAG010   |                      | @requestId | @testStartTime | <productType> |
    Then I get "<rc>" result code
    Examples:
      | clientId       | apiKey       | productType                           | rc     |
      # Il productType è presente nella configurazione di ExternalChannel PnEcDuplicatesCheck
       | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     | 400.02 |
      # Il productType non è presente in PnEcDuplicatesCheck
      | @clientId-cons | @apiKey-cons | @productType_not_for_duplicates_check | 200.00 |

  @PnEcSendMessage @PAPER @validaCourier
  Scenario Outline: Verifica la valorizzazione del courier:
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When I send the following paper progress status requests:
      | statusCode   | deliveryFailureCause   | courier   | iun   | statusDateTime   | clientRequestTimestamp   |
      | <statusCode> | <deliveryFailureCause> | <courier> | <iun> | <statusDateTime> | <clientRequestTimestamp> |
    Then I get "<courier>" courier and I get "<statusCode>" statusCode:
    Examples:
      | clientId       | apiKey       | statusCode | deliveryFailureCause | courier  | iun        | statusDateTime           | clientRequestTimestamp   | rc     |
      | @clientId-cons | @apiKey-cons | CON080     |                      | XXXXX    |            | @now                     | @now                     | 200.00 |
      | @clientId-cons | @apiKey-cons | RECRS002A  | M02                  |          | @requestId | @now                     | @now                     | 200.00 |
