Feature: /io-connector/activations — Attivazione e disattivazione del servizio SEND su App IO

  @invioIO @ioActivation @ioActivation_ok @ioActivation_consensi
  Scenario Outline: Attivazione IO con utente PF — risposta 200 e consensi TOS e DATAPRIVACY registrati
    Given un utente auth-fleet con cxType "PF" e uid generato
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "ACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "INACTIVE"
    And il fiscal_code nella risposta corrisponde a quello inviato
    And il consenso "TOS" dell'utente risulta accettato nella versione attesa
    And il consenso "DATAPRIVACY" dell'utente risulta accettato nella versione attesa
    Examples:
      | fiscalCode                                |
      | @io.activation.fiscalCode.notWhitelisted |

  @invioIO @ioActivation @ioActivation_ok
  Scenario Outline: Disattivazione IO con utente PF — risposta 200 e nessun consenso registrato
    Given un utente auth-fleet con cxType "PF" e uid generato
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "INACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "INACTIVE"
    And il consenso "TOS" dell'utente non risulta registrato
    And il consenso "DATAPRIVACY" dell'utente non risulta registrato
    Examples:
      | fiscalCode                                |
      | @io.activation.fiscalCode.notWhitelisted |

  @invioIO @ioActivation @ioActivation_ok @ioActivation_consensi
  Scenario Outline: Attivazione IO ripetuta con lo stesso utente — risposta 200 e consensi ancora accettati
    Given un utente auth-fleet con cxType "PF" e uid generato
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "ACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "INACTIVE"
    And il consenso "TOS" dell'utente risulta accettato nella versione attesa
    And il consenso "DATAPRIVACY" dell'utente risulta accettato nella versione attesa
    Examples:
      | fiscalCode                                |
      | @io.activation.fiscalCode.notWhitelisted |

  @invioIO @ioActivation @ioActivation_ok @ioActivation_lettura
  Scenario Outline: Lettura attivazione IO — risposta 200 con status INACTIVE
    Given una richiesta di lettura attivazione IO con fiscal_code "<fiscalCode>"
    When invio la richiesta di lettura attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "INACTIVE"
    And il fiscal_code nella risposta corrisponde a quello inviato
    Examples:
      | fiscalCode                                |
      | @io.activation.fiscalCode.notWhitelisted |

  @invioIO @ioActivation @ioActivation_ko
  Scenario Outline: Attivazione IO con utente BO — risposta 500 PN_IOCONNECTOR_ACTIVATION_ERROR
    Given un utente auth-fleet con cxType "BO" e uid generato
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "ACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 500
    And la risposta contiene il codice di errore "PN_IOCONNECTOR_ACTIVATION_ERROR"
    Examples:
      | fiscalCode                                |
      | @io.activation.fiscalCode.notWhitelisted |

  @invioIO @ioActivation @ioActivation_ok
  Scenario Outline: Disattivazione IO con utente BO — risposta 200
    Given un utente auth-fleet con cxType "BO" e uid generato
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "INACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "INACTIVE"
    Examples:
      | fiscalCode                                |
      | @io.activation.fiscalCode.notWhitelisted |

  @invioIO @ioActivation @ioActivation_ko @ioActivation_ko_header_mancante
  Scenario Outline: Attivazione IO senza header obbligatorio <header> — risposta 400
    Given un utente auth-fleet con cxType "PF" e uid generato
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "ACTIVE"
    When invio la richiesta di attivazione IO senza l'header "<header>"
    Then la risposta HTTP ha status 400
    Examples:
      | header              | fiscalCode                                |
      | x-pagopa-pn-uid     | @io.activation.fiscalCode.notWhitelisted |
      | x-pagopa-pn-cx-type | @io.activation.fiscalCode.notWhitelisted |
      | x-pagopa-pn-cx-id   | @io.activation.fiscalCode.notWhitelisted |

  @invioIO @ioActivation @ioActivation_ko @ioActivation_ko_campo_mancante
  Scenario Outline: Attivazione IO senza campo obbligatorio <campo> — risposta 400
    Given un utente auth-fleet con cxType "PF" e uid generato
    And una richiesta di attivazione IO senza il campo "<campo>"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 400
    Examples:
      | campo       |
      | fiscal_code |
      | status      |

  @invioIO @ioActivation @ioActivation_ko @ioActivation_ko_campo_non_valido
  Scenario Outline: Attivazione IO con valore non valido <valore> — risposta 500
    Given un utente auth-fleet con cxType "<cxType>" e uid generato
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "<status>"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 500
    And la risposta contiene il codice di errore "PN_GENERIC_ERROR"
    Examples:
      | valore         | cxType | status  | fiscalCode                                |
      | status UNKNOWN | PF     | UNKNOWN | @io.activation.fiscalCode.notWhitelisted |
      | cxType XX      | XX     | ACTIVE  | @io.activation.fiscalCode.notWhitelisted |

  @invioIO @ioActivation @ioActivation_ko @ioActivation_lettura
  Scenario: Lettura attivazione IO senza fiscal_code — risposta 400
    Given una richiesta di lettura attivazione IO senza fiscal_code
    When invio la richiesta di lettura attivazione IO
    Then la risposta HTTP ha status 400

  @invioIO @ioActivation @ioActivation_ko @ioActivation_lettura
  Scenario Outline: Lettura attivazione IO con fiscal_code di lunghezza non valida — risposta 400
    Given una richiesta di lettura attivazione IO con fiscal_code "<fiscalCode>"
    When invio la richiesta di lettura attivazione IO
    Then la risposta HTTP ha status 400
    Examples:
      | fiscalCode        |
      | TSTIOC80A01H501   |
      | TSTIOC80A01H501ZZ |
