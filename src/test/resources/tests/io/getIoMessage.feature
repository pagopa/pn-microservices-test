Feature: GET /messages/{id} — Recupero metadati allegati messaggio IO

  @wip @invioIO @getMessage @getMessage_ok
  Scenario Outline: Recupero metadati messaggio IO esistente — risposta 200
    Given un ioMessageId "<ioMessageId>"
    When recupero il messaggio IO per id
    Then la risposta HTTP ha status 200
    And la risposta contiene i dettagli del messaggio
    And la risposta contiene la lista degli allegati
    Examples:
      | ioMessageId     |
      | @io.ioMessageId |

  @wip @invioIO @getMessage @getMessage_notFound
  Scenario Outline: Recupero messaggio IO con id inesistente — risposta 404
    Given un ioMessageId "<ioMessageId>"
    When recupero il messaggio IO per id
    Then la risposta HTTP ha status 404
    Examples:
      | ioMessageId                |
      | IO-MSG-ID-NOT-EXISTING-XYZ |

  @wip @invioIO @getMessage @getMessage_forbidden
  Scenario Outline: Recupero messaggio IO con codice fiscale del chiamante non coerente — risposta 403
    Given un ioMessageId "<ioMessageId>"
    When recupero il messaggio IO per id
    Then la risposta HTTP ha status 403
    Examples:
      | ioMessageId             |
      | @io.ioMessageId.wrongCf |
