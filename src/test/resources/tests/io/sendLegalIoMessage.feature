Feature: POST /io-connector/sendmessage — Invio messaggio legale su App IO (PN-21718)

  @invioIO @sendLegalIoMessage @sendLegal_courtesy @smokeTest
  Scenario Outline: Invio del messaggio legale a utente con SEND attivo su IO — SENT_COURTESY
    Given una richiesta di invio messaggio legale IO valida per il destinatario "<taxId>"
    When invio il messaggio legale IO
    Then la risposta HTTP ha status 200
    And l'esito dell'invio è "SENT_COURTESY"
    #se lo stato è SENT_OPTIN vuol dire che il destinatario ha disattivato SEND
    And la risposta contiene l'id del messaggio IO
    And il requestId nella risposta corrisponde a quello inviato
    Examples:
      | taxId                    |
      | @io.legal.taxId.pnActive |

  @invioIO @sendLegalIoMessage @sendLegal_optin @ioOptIn
  Scenario Outline: Invio dell'opt-in a utente con SEND non attivo su IO — SENT_OPTIN
    Given una richiesta di invio messaggio legale IO valida per il destinatario "<taxId>"
    When invio il messaggio legale IO
    Then la risposta HTTP ha status 200
    And l'esito dell'invio è "SENT_OPTIN"
    #se lo stato risulta NOT_SENT_OPTIN_ALREADY_SENT" vuol dire che l'optin è già stato inviato
    #se lo stato risulta SENT_COURTESY vuol dire che l'utente ha attivato SEND e quindi riceve la notifica
    And la risposta contiene l'id del messaggio IO
    And il requestId nella risposta corrisponde a quello inviato
    And nella tabella opt-in esiste il record del destinatario aggiornato dopo l'invio
    Examples:
      | taxId                       |
      | @io.legal.taxId.pnNotActive |
