Feature: POST /io/profile — Verifica raggiungibilità profilo IO

  Background:
    Given il cxId è "@clientId-delivery-push"

  @invioIO @getIOProfile @getIOProfile_ok @smokeTest
  Scenario Outline: Verifica profilo IO con destinatario raggiungibile — risposta 200 SENDER_ALLOWED
    Given una richiesta profilo IO valida con recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>"
    When invio la richiesta di profilo IO
    Then la risposta HTTP ha status 200
    And lo status del profilo è "SENDER_ALLOWED"
    And la risposta contiene il campo preferredLanguages
    Examples:
      | recipientTaxId       | senderServiceId       |
      | @io.recipientTaxId   | @io.senderServiceId   |

  @invioIO @getIOProfile @getIOProfile_not_allowed
  Scenario Outline: Verifica profilo IO con destinatario non raggiungibile — risposta 200 SENDER_NOT_ALLOWED
    Given una richiesta profilo IO valida con recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>"
    When invio la richiesta di profilo IO
    Then la risposta HTTP ha status 200
    And lo status del profilo è "SENDER_NOT_ALLOWED"
    And la risposta non contiene il campo preferredLanguages
    Examples:
      | recipientTaxId              | senderServiceId       |
      | @io.recipientTaxId.notOnIo  | @io.senderServiceId   |

  @invioIO @getIOProfile @getIOProfile_ko @getIOProfile_ko_campo_mancante
  Scenario Outline: Verifica profilo IO senza campo obbligatorio <campo> — risposta 400
    Given una richiesta profilo IO senza il campo "<campo>"
    When invio la richiesta di profilo IO
    Then la risposta HTTP ha status 400
    Examples:
      | campo           |
      | recipientTaxId  |
      | senderServiceId |

  @invioIO @getIOProfile @getIOProfile_ko @getIOProfile_ko_campo_mancante
  Scenario Outline: Verifica profilo IO con payload vuoto — risposta <statusAtteso>
    Given una richiesta profilo IO senza campi obbligatori
    When invio la richiesta di profilo IO
    Then la risposta HTTP ha status <statusAtteso>
    Examples:
      | statusAtteso |
      | 400          |

  @invioIO @getIOProfile @getIOProfile_ko @getIOProfile_ko_header_mancante
  Scenario Outline: Verifica profilo IO senza header obbligatorio x-pagopa-iocon-cx-id — risposta 400
    Given una richiesta profilo IO valida con recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>"
    When invio la richiesta di profilo IO senza l'header obbligatorio
    Then la risposta HTTP ha status 400
    Examples:
      | recipientTaxId       | senderServiceId       |
      | @io.recipientTaxId   | @io.senderServiceId   |
