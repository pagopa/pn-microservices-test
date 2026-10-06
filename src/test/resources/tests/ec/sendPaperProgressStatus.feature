Feature: Send Paper Progress Status

  Background:
    Given a "@clientId-cons" and "@channel_paper" to send on
    When "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following attachments:
      | documentType  | fileName                    | mimeType        |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf |
    * try to send a paper message
    * waiting for scheduling
    Then wait for the message to be sent

  @PnEcSendMessage @PAPER @complete
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following paper progress status event attachments:
      | documentType  | fileName                    | mimeType        | attachmentDocumentType |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf | AR                     |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | courier  | sourceType | originType |
      | CON080     |                      | @requestId | @now           |          | SCANNED    | DUPLICATED |
      | RECAG004   |                      | @requestId | @now           | YXYXYXYX | SCANNED    | DUPLICATED |
    Then check if paper progress status requests have been accepted
    And the "CON080" event attachments have "SCANNED" sourceType and "DUPLICATED" originType
    And the "RECAG004" event attachments have "SCANNED" sourceType and "DUPLICATED" originType
    And the paper status pull is consistent with the last stored event
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
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType   | courier    | sourceType    | originType    |
      | RECAG010   |                      | @requestId | @testStartTime | <productType> | <courier1> | <sourceType1> | <originType1> |
    And wait for the request to have status "RECAG010"
    And I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType   | courier    | sourceType    | originType    |
      | RECAG010   |                      | @requestId | @testStartTime | <productType> | <courier2> | <sourceType2> | <originType2> |
    Then I get "<rc>" result code
    Examples:
      | clientId       | apiKey       | productType                           | courier1        | courier2        | sourceType1 | originType1 | sourceType2 | originType2 | rc     |
      # Il productType è presente nella configurazione di ExternalChannel PnEcDuplicatesCheck
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     | @paper.courier1 | @paper.courier2 |             |             |             |             | 400.02 |
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     |                 | @paper.courier2 |             |             |             |             | 400.02 |
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     | @paper.courier1 | @paper.courier1 |             |             |             |             | 400.02 |
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     |                 |                 |             |             |             |             | 400.02 |
      # Il productType non è presente in PnEcDuplicatesCheck
      | @clientId-cons | @apiKey-cons | @productType_not_for_duplicates_check | @paper.courier1 | @paper.courier2 |             |             |             |             | 200.00 |
      # stessi sourceType/originType => resta un duplicato
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     | @paper.courier1 | @paper.courier1 | SCANNED     | DUPLICATED  | SCANNED     | DUPLICATED  | 400.02 |
      # sourceType diverso => non è più un duplicato
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     | @paper.courier1 | @paper.courier1 | SCANNED     | ORIGINAL    | DIGITAL     | ORIGINAL    | 200.00 |
      # originType diverso => non è più un duplicato
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     | @paper.courier1 | @paper.courier1 | DIGITAL     | ORIGINAL    | DIGITAL     | DUPLICATED  | 200.00 |
      # campi valorizzati solo sul secondo evento => non è un duplicato
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     | @paper.courier1 | @paper.courier1 |             |             | SCANNED     | ORIGINAL    | 200.00 |

    #per gli allegati multipli è stato fatto un test puntuale con una chiamata postman
  @PnEcSendMessage @PAPER @verificaDuplicati @MultipleAttachments
  Scenario Outline: Controllo su eventi duplicati nell'avanzamento dei progressi di postalizzazione
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following paper progress status event attachments:
      | documentType  | fileName                    | mimeType        | attachmentDocumentType |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf | AR                     |
      | @doc_type_aar | src/test/resources/test_pdf.pdf | application/pdf | AR                     |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType   | sourceType    | originType    |
      | RECAG010   |                      | @requestId | @testStartTime | <productType> | <sourceType1> | <originType1> |
    And wait for the request to have status "RECAG010"
    And I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType   | sourceType    | originType    |
      | RECAG010   |                      | @requestId | @testStartTime | <productType> | <sourceType2> | <originType2> |
    Then I get "<rc>" result code
    Examples:
      | clientId       | apiKey       | productType                           | sourceType1 | originType1 | sourceType2 | originType2 | rc     |
      # Il productType è presente nella configurazione di ExternalChannel PnEcDuplicatesCheck
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     |             |             |             |             | 400.02 |
      # Il productType non è presente in PnEcDuplicatesCheck
      | @clientId-cons | @apiKey-cons | @productType_not_for_duplicates_check |             |             |             |             | 200.00 |
      # con più allegati il confronto è per-allegato, un sourceType diverso rompe il duplicato
      | @clientId-cons | @apiKey-cons | @productType_for_duplicates_check     | SCANNED     | ORIGINAL    | DIGITAL     | ORIGINAL    | 200.00 |

  @PnEcSendMessage @PAPER @validaCourier
  Scenario Outline: Verifica la valorizzazione del courier:
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When I send the following paper progress status requests:
      | statusCode   | deliveryFailureCause   | courier   | iun   | statusDateTime   | clientRequestTimestamp   |
      | <statusCode> | <deliveryFailureCause> | <courier> | <iun> | <statusDateTime> | <clientRequestTimestamp> |
    And wait for the request to have status "<statusCode>"
    Then I get "<courier>" courier and I get "<statusCode>" statusCode:
    Examples:
      | clientId       | apiKey       | statusCode | deliveryFailureCause | courier  | iun        | statusDateTime           | clientRequestTimestamp   |
      | @clientId-cons | @apiKey-cons | CON080     |                      | XXXXX    |            | @now                     | @now                     |
      | @clientId-cons | @apiKey-cons | RECRS002A  | M02                  |          | @requestId | @now                     | @now                     |

  @PnEcSendMessage @PAPER @nuoviStatusCode
  Scenario Outline: Accettazione dei nuovi statusCode della Gara Consolidatore FASE2 e avanzamento dello stato della richiesta
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When I send the following paper progress status requests:
      | statusCode   | deliveryFailureCause | iun        | statusDateTime |
      | <statusCode> |                      | @requestId | @now           |
    # l'evento deve essere accettato in ingresso e lo stato deve avanzare sulla macchina a stati
    Then check if paper progress status requests have been accepted
    Examples:
      | clientId       | apiKey       | statusCode |
      | @clientId-cons | @apiKey-cons | REC016     |
      | @clientId-cons | @apiKey-cons | REC018     |
      | @clientId-cons | @apiKey-cons | REC991     |
      | @clientId-cons | @apiKey-cons | RECAG010A  |
      | @clientId-cons | @apiKey-cons | CON09B     |

  @PnEcSendMessage @PAPER @deliveryFailureCauseM10
  Scenario Outline: Accettazione della deliveryFailureCause M10 (indirizzo non leggibile) sui mancati recapiti
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When I send the following paper progress status requests:
      | statusCode   | deliveryFailureCause | iun        | statusDateTime |
      | <statusCode> | M10                  | @requestId | @now           |
    Then I get "200.00" result code
    Examples:
      | clientId       | apiKey       | statusCode |
      # M10 deve essere censita sia nel codice di pn-ec sia, per gli statusCode configurati, nel parametro SSM pn-EC-esitiCartaceo
      | @clientId-cons | @apiKey-cons | RECRS002A  |
      | @clientId-cons | @apiKey-cons | RECRS002B  |
      | @clientId-cons | @apiKey-cons | RECRS002C  |
      | @clientId-cons | @apiKey-cons | RECRN002A  |
      | @clientId-cons | @apiKey-cons | RECRN002B  |
      | @clientId-cons | @apiKey-cons | RECRN002C  |
      | @clientId-cons | @apiKey-cons | RECAG003A  |
      | @clientId-cons | @apiKey-cons | RECAG003B  |
      | @clientId-cons | @apiKey-cons | RECAG003C  |

  @PnEcSendMessage @PAPER @printerDu
  Scenario Outline: Memorizzazione di printer e du ricevuti negli eventi di avanzamento della Gara Consolidatore FASE2
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | printer   | du   |
      | CON080     |                      | @requestId | @now           | <printer> | <du> |
    Then check if paper progress status requests have been accepted
    And the "CON080" event has "<printer>" printer and "<du>" du
    And the paper status pull is consistent with the last stored event
    Examples:
      | clientId       | apiKey       | printer      | du             |
      | @clientId-cons | @apiKey-cons | 123456789abc | DU123456789abc |
      # campi assenti => l'evento resta valido e non vengono valorizzati
      | @clientId-cons | @apiKey-cons |              |                |

  @PnEcSendMessage @PAPER @verificaDuplicati @printerDu
  Scenario Outline: Controllo su eventi duplicati con printer e du nell'avanzamento dei progressi di postalizzazione
    Given the ExternalChannel client "<clientId>" authenticated by "<apiKey>"
    And "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following paper progress status event attachments:
      | documentType  | fileName                    | mimeType        | attachmentDocumentType |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf | AR                     |
    When I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType                       | courier         | printer    | du    |
      | RECAG010   |                      | @requestId | @testStartTime | @productType_for_duplicates_check | @paper.courier1 | <printer1> | <du1> |
    And wait for the request to have status "RECAG010"
    And I send the following paper progress status requests:
      | statusCode | deliveryFailureCause | iun        | statusDateTime | productType                       | courier         | printer    | du    |
      | RECAG010   |                      | @requestId | @testStartTime | @productType_for_duplicates_check | @paper.courier1 | <printer2> | <du2> |
    Then I get "<rc>" result code
    Examples:
      | clientId       | apiKey       | printer1     | du1            | printer2     | du2            | rc     |
      # stessi printer/du => resta un duplicato
      | @clientId-cons | @apiKey-cons | 123456789abc | DU123456789abc | 123456789abc | DU123456789abc | 400.02 |
      # printer diverso => non è più un duplicato
      | @clientId-cons | @apiKey-cons | 123456789abc | DU123456789abc | 987654321cba | DU123456789abc | 200.00 |
      # du diverso => non è più un duplicato
      | @clientId-cons | @apiKey-cons | 123456789abc | DU123456789abc | 123456789abc | DU987654321cba | 200.00 |
      # campi valorizzati solo sul secondo evento => non è un duplicato
      | @clientId-cons | @apiKey-cons |              |                | 123456789abc | DU123456789abc | 200.00 |
