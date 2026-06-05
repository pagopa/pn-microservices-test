Feature: GET /io-connector/messages/{id}/{url} — Download allegato messaggio IO

  Background:
    Given il cxId è "@clientId-delivery-push"

  @invioIO @getAttachment @getAttachment_404_not_found
  Scenario Outline: Download allegato con requestId inesistente — risposta 404
    Given un requestId "<requestId>"
    When recupero l'allegato IO con fileKey "<fileKey>" e taxId "@io.recipientTaxId"
    Then la risposta HTTP ha status 404
    Examples:
      | requestId                  | fileKey                                   |
      | MSG-NOT-EXISTING-XYZ-00000 | PN_AAR-10b382bdd1a74bfb863918949e8f54f9.pdf |

  @invioIO @getAttachment @getAttachment_404_wrong_fileKey
  Scenario Outline: Download allegato con fileKey non presente nel messaggio — risposta 404
    Given invio messaggio IO valido con allegati, recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero l'allegato IO con fileKey "PN_NOTIFICATION_ATTACHMENTS-WRONG-KEY.pdf" e taxId "<recipientTaxId>"
    Then la risposta HTTP ha status 404
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |

  @invioIO @getAttachment @getAttachment_404_wrong_taxId
  Scenario Outline: Download allegato con taxId non coerente con il messaggio — risposta 404
    Given invio messaggio IO valido con allegati, recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero l'allegato IO con la prima fileKey e taxId errato
    Then la risposta HTTP ha status 404
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |

  @invioIO @getAttachment @getAttachment_403_header_mancante @ignore
  Scenario Outline: Download allegato senza header obbligatorio x-pagopa-pn-cx-id — risposta 403
    Given invio messaggio IO valido con allegati, recipientTaxId "<recipientTaxId>" e senderServiceId "<senderServiceId>"
    When recupero l'allegato IO senza l'header obbligatorio
    Then la risposta HTTP ha status 403
    Examples:
      | recipientTaxId       | senderServiceId     |
      | @io.recipientTaxId   | @io.senderServiceId |