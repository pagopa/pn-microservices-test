Feature: GET /messages/{id} — Recupero metadati allegati messaggio IO

  Background:
    Given il cxId è "@clientId-delivery-push"

  @invioIO @getMessage @getMessage_ok @smokeTest
  Scenario Outline: Recupero metadati messaggio IO esistente — risposta 200
    Given ho inviato un messaggio IO valido con recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero il messaggio IO per id
    Then la risposta HTTP ha status 200
    And la risposta contiene i dettagli del messaggio
    And la risposta contiene la lista degli allegati
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |

  @invioIO @getMessage @getMessage_notFound
  Scenario: Recupero messaggio IO con requestId inesistente — risposta 404
    Given un ioMessageId "MSG-NOT-EXISTING-XYZ-00000"
    When recupero il messaggio IO per id
    Then la risposta HTTP ha status 404

  @invioIO @getMessage @getMessage_notFound_wrongCf
  Scenario Outline: Recupero messaggio IO con codice fiscale non coerente — risposta 404
    Given ho inviato un messaggio IO valido con recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero il messaggio IO per id con taxId errato
    Then la risposta HTTP ha status 404
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |

  @invioIO @getMessage @getMessage_attachments
  Scenario Outline: Recupero metadati messaggio IO con allegati — risposta 200 con lista allegati non vuota
    Given ho inviato un messaggio IO valido con allegati, recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero il messaggio IO per id
    Then la risposta HTTP ha status 200
    And la risposta contiene i dettagli del messaggio
    And la risposta contiene almeno un allegato con fileKey e category
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |
