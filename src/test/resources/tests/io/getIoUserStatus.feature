Feature: POST /io-connector/user-status — Stato dell'utente su App IO e servizio SEND (PN-21718)

  @invioIO @getIoUserStatus @getIoUserStatus_ok
  Scenario Outline: Stato utente <descrizione> — <status>
    Given una richiesta di stato utente IO per il codice fiscale "<taxId>"
    When richiedo lo stato utente IO
    Then la risposta HTTP ha status 200
    And lo status del profilo è "<status>"
    And il taxId nella risposta corrisponde a quello richiesto
    Examples:
      | descrizione                   | taxId                          | status           |
      | con SEND attivo               | @io.legal.taxId.pnActive       | PN_ACTIVE        |
      | su IO con SEND non attivo     | @io.legal.taxId.pnNotActive    | PN_NOT_ACTIVE    |
      #Disttivare SEND su questo CF per testare lo scenario con SEND disattivo se risponde PN_ACTIVE
      | fuori whitelist               | @io.legal.taxId.notWhitelisted | APPIO_NOT_ACTIVE |

  @invioIO @getIoUserStatus @getIoUserStatus_ko
  Scenario: Stato utente senza taxId — risposta 400
    Given una richiesta di stato utente IO senza taxId
    When richiedo lo stato utente IO
    Then la risposta HTTP ha status 400

  @invioIO @getIoUserStatus @getIoUserStatus_ko
  Scenario Outline: Stato utente con taxId di lunghezza non valida "<taxId>" — risposta 400
    Given una richiesta di stato utente IO per il codice fiscale "<taxId>"
    When richiedo lo stato utente IO
    Then la risposta HTTP ha status 400
    Examples:
      | taxId              |
      | RSSMRA80A01H501    |
      | RSSMRA80A01H501UXX |
