Feature: POST /io/profile — Verifica raggiungibilità profilo IO

  Background:
    Given il cxId è "@clientId-delivery-push"

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
    Given una richiesta profilo IO valida con recipientTaxId "<recipientTaxId>", senderTaxId "<senderTaxId>", senderServiceId "<senderServiceId>"
    When invio la richiesta di profilo IO senza l'header obbligatorio
    Then la risposta HTTP ha status 400
    Examples:
      | recipientTaxId       | senderTaxId      | senderServiceId       |
      | @io.recipientTaxId   | @io.senderTaxId  | @io.senderServiceId   |
