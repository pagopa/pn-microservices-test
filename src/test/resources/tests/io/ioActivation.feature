Feature: /io-connector/activations — Attivazione e disattivazione del servizio SEND su App IO

  @invioIO @ioActivation @ioActivation_ok @ioActivation_consensi
  Scenario Outline: Attivazione IO con utente <cxType> — risposta 200 e consensi TOS e DATAPRIVACY registrati
    Given un utente auth-fleet con cxType "<cxType>" e uid generato
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "ACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "INACTIVE"
    And il fiscal_code nella risposta corrisponde a quello inviato
    And il consenso "TOS" dell'utente risulta accettato nella versione attesa
    And il consenso "DATAPRIVACY" dell'utente risulta accettato nella versione attesa
    Examples:
      | cxType | fiscalCode                                |
      | PF     | @io.activation.fiscalCode.notWhitelisted |
      | PG     | @io.activation.fiscalCode.notWhitelisted |

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

  @invioIO @ioActivation @ioActivation_ko @ioActivation_ko_cxtype_non_ammesso
  Scenario Outline: Attivazione IO con cxType non ammesso <cxType> e status <status> — risposta 400
    Given un utente auth-fleet con cxType "<cxType>" e uid generato
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "<status>"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 400
    And la risposta contiene il codice di errore "PN_GENERIC_INVALIDPARAMETER"
    Examples:
      | cxType | status   | fiscalCode                                |
      | PA     | ACTIVE   | @io.activation.fiscalCode.notWhitelisted |
      | PA     | INACTIVE | @io.activation.fiscalCode.notWhitelisted |
      | RADD   | ACTIVE   | @io.activation.fiscalCode.notWhitelisted |
      | RADD   | INACTIVE | @io.activation.fiscalCode.notWhitelisted |
      | BO     | ACTIVE   | @io.activation.fiscalCode.notWhitelisted |
      | BO     | INACTIVE | @io.activation.fiscalCode.notWhitelisted |
      | XX     | ACTIVE   | @io.activation.fiscalCode.notWhitelisted |

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

  # Escluso con @ignore: verifica il ramo verso IO e richiede l'aggiornamento di pn-io-connector (WI 9 della SRS: rimozione
  # della whitelist legale, client legale puntato al mock). Dopo l'aggiornamento rimuovere @ignore e decommentare i tag.
  @ignore
  # @invioIO @ioActivation @ioActivation_ok @ioActivation_consensi @ioActivationMockIo
  Scenario Outline: Attivazione IO con utente in whitelist — IO conferma ACTIVE
    Given un utente auth-fleet con cxType "PF" e uid generato
    And IO risponde all'aggiornamento dell'attivazione con status "ACTIVE"
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "ACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "ACTIVE"
    And la version dell'attivazione è 1
    And il fiscal_code nella risposta corrisponde a quello inviato
    And il consenso "TOS" dell'utente risulta accettato nella versione attesa
    And il consenso "DATAPRIVACY" dell'utente risulta accettato nella versione attesa
    And IO ha ricevuto la richiesta di aggiornamento dell'attivazione con status "ACTIVE"
    And IO non ha ricevuto richieste di lettura dell'attivazione
    Examples:
      | fiscalCode                             |
      | @io.activation.fiscalCode.whitelisted |

  # Escluso con @ignore: verifica il ramo verso IO e richiede l'aggiornamento di pn-io-connector (WI 9 della SRS: rimozione
  # della whitelist legale, client legale puntato al mock). Dopo l'aggiornamento rimuovere @ignore e decommentare i tag.
  @ignore
  # @invioIO @ioActivation @ioActivation_ok @ioActivationMockIo
  Scenario Outline: Disattivazione IO con utente in whitelist — IO conferma INACTIVE
    Given un utente auth-fleet con cxType "PF" e uid generato
    And IO risponde all'aggiornamento dell'attivazione con status "INACTIVE"
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "INACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "INACTIVE"
    And il consenso "TOS" dell'utente non risulta registrato
    And il consenso "DATAPRIVACY" dell'utente non risulta registrato
    And IO ha ricevuto la richiesta di aggiornamento dell'attivazione con status "INACTIVE"
    Examples:
      | fiscalCode                             |
      | @io.activation.fiscalCode.whitelisted |

  # Escluso con @ignore: verifica il ramo verso IO e richiede l'aggiornamento di pn-io-connector (WI 9 della SRS: rimozione
  # della whitelist legale, client legale puntato al mock). Dopo l'aggiornamento rimuovere @ignore e decommentare i tag.
  @ignore
  # @invioIO @ioActivation @ioActivation_ok @ioActivationMockIo
  Scenario Outline: Attivazione IO con utente in whitelist — IO risponde PENDING
    Given un utente auth-fleet con cxType "PF" e uid generato
    And IO risponde all'aggiornamento dell'attivazione con status "PENDING"
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "ACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "PENDING"
    And IO ha ricevuto la richiesta di aggiornamento dell'attivazione con status "ACTIVE"
    Examples:
      | fiscalCode                             |
      | @io.activation.fiscalCode.whitelisted |

  # Escluso con @ignore: verifica il ramo verso IO e richiede l'aggiornamento di pn-io-connector (WI 9 della SRS: rimozione
  # della whitelist legale, client legale puntato al mock). Dopo l'aggiornamento rimuovere @ignore e decommentare i tag.
  @ignore
  # @invioIO @ioActivation @ioActivation_ok @ioActivation_fallback @ioActivationMockIo
  Scenario Outline: Attivazione IO con aggiornamento su IO in errore — risposta con lo stato letto da IO
    Given un utente auth-fleet con cxType "PF" e uid generato
    And IO risponde all'aggiornamento dell'attivazione con errore 500
    And IO risponde alla lettura dell'attivazione con status "ACTIVE"
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "ACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "ACTIVE"
    And IO ha ricevuto la richiesta di aggiornamento dell'attivazione con status "ACTIVE"
    And IO ha ricevuto la richiesta di lettura dell'attivazione
    Examples:
      | fiscalCode                             |
      | @io.activation.fiscalCode.whitelisted |

  # Escluso con @ignore: verifica il ramo verso IO e richiede l'aggiornamento di pn-io-connector (WI 9 della SRS: rimozione
  # della whitelist legale, client legale puntato al mock). Dopo l'aggiornamento rimuovere @ignore e decommentare i tag.
  @ignore
  # @invioIO @ioActivation @ioActivation_ko @ioActivation_fallback @ioActivationMockIo
  Scenario Outline: Attivazione IO con aggiornamento e lettura su IO in errore — risposta 500 PN_IOCONNECTOR_ACTIVATION_ERROR
    Given un utente auth-fleet con cxType "PF" e uid generato
    And IO risponde all'aggiornamento dell'attivazione con errore 500
    And IO risponde alla lettura dell'attivazione con errore 500
    And una richiesta di attivazione IO con fiscal_code "<fiscalCode>" e status "ACTIVE"
    When invio la richiesta di attivazione IO
    Then la risposta HTTP ha status 500
    And la risposta contiene il codice di errore "PN_IOCONNECTOR_ACTIVATION_ERROR"
    And il consenso "TOS" dell'utente risulta accettato nella versione attesa
    And il consenso "DATAPRIVACY" dell'utente risulta accettato nella versione attesa
    And IO ha ricevuto la richiesta di lettura dell'attivazione
    Examples:
      | fiscalCode                             |
      | @io.activation.fiscalCode.whitelisted |

  # Escluso con @ignore: verifica il ramo verso IO e richiede l'aggiornamento di pn-io-connector (WI 9 della SRS: rimozione
  # della whitelist legale, client legale puntato al mock). Dopo l'aggiornamento rimuovere @ignore e decommentare i tag.
  @ignore
  # @invioIO @ioActivation @ioActivation_ko @ioActivation_lettura @ioActivationMockIo
  Scenario Outline: Lettura attivazione IO per utente non presente su IO — risposta 404
    Given IO risponde alla lettura dell'attivazione con errore 404
    And una richiesta di lettura attivazione IO con fiscal_code "<fiscalCode>"
    When invio la richiesta di lettura attivazione IO
    Then la risposta HTTP ha status 404
    And IO ha ricevuto la richiesta di lettura dell'attivazione
    Examples:
      | fiscalCode                             |
      | @io.activation.fiscalCode.whitelisted |

  # Escluso con @ignore: verifica il ramo verso IO e richiede l'aggiornamento di pn-io-connector (WI 9 della SRS: rimozione
  # della whitelist legale, client legale puntato al mock). Dopo l'aggiornamento rimuovere @ignore e decommentare i tag.
  @ignore
  # @invioIO @ioActivation @ioActivation_ok @ioActivation_lettura @ioActivationMockIo
  Scenario Outline: Lettura attivazione IO con stato PENDING su IO — risposta 200 PENDING
    Given IO risponde alla lettura dell'attivazione con status "PENDING"
    And una richiesta di lettura attivazione IO con fiscal_code "<fiscalCode>"
    When invio la richiesta di lettura attivazione IO
    Then la risposta HTTP ha status 200
    And lo status dell'attivazione è "PENDING"
    And il fiscal_code nella risposta corrisponde a quello inviato
    And IO ha ricevuto la richiesta di lettura dell'attivazione
    Examples:
      | fiscalCode                             |
      | @io.activation.fiscalCode.whitelisted |
