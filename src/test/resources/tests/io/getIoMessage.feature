Feature: GET /messages/{id} — Recupero metadati allegati messaggio IO

  Background:
    Given il cxId è "@clientId-delivery-push"

  @invioIO @getMessage @getMessage_ok @smokeTest
  Scenario Outline: Recupero metadati messaggio IO esistente — risposta 200
    Given invio messaggio IO valido con recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero il messaggio IO per id
    Then la risposta HTTP ha status 200
    And la risposta contiene i dettagli del messaggio con subject e markdown corretti
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |

  @invioIO @getMessage @getMessage_notFound
  Scenario Outline: Recupero messaggio IO con requestId inesistente — risposta 404
    Given un requestId "<requestId>"
    When recupero il messaggio IO per id
    Then la risposta HTTP ha status 404
    Examples:
      | requestId                  |
      | MSG-NOT-EXISTING-XYZ-00000 |

  @invioIO @getMessage @getMessage_notFound_wrongCf
  Scenario Outline: Recupero messaggio IO con codice fiscale non coerente — risposta 404
    Given invio messaggio IO valido con recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero il messaggio IO per id con taxId errato
    Then la risposta HTTP ha status 404
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |

  @invioIO @getMessage @getMessage_attachments
  Scenario Outline: Recupero metadati messaggio IO con allegati PDF — risposta 200 con lista allegati valorizzata
    Given invio messaggio IO valido con allegati, recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero il messaggio IO per id
    Then la risposta HTTP ha status 200
    And la risposta contiene i dettagli del messaggio con subject e markdown corretti
    And la risposta contiene almeno un allegato con fileKey, category e contentType
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |

  @invioIO @getMessage @getMessage_ko_header_mancante
  Scenario Outline: Recupero messaggio IO senza header obbligatorio x-pagopa-pn-cx-id — risposta 400
    Given invio messaggio IO valido con recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero il messaggio IO senza l'header obbligatorio
    Then la risposta HTTP ha status 400
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |
