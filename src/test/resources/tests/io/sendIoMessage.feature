Feature: POST /io/message — Presa in carico sincrona

  Background:
    Given il cxId è "@clientId-delivery-push"

  @invioIO @postMessage @invioIO_accepted @smokeTest
  Scenario Outline: Invio messaggio IO e verifica accettazione
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    And lo status del messaggio è "ACCEPTED"
    And il requestId nella risposta corrisponde a quello inviato
    And il cxId nella risposta corrisponde a quello inviato
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_accepted @invioIO_no_iun
  Scenario Outline: Invio messaggio IO senza iun (campo opzionale) — risposta 200 ACCEPTED
    Given un messaggio IO valido senza iun con recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    And lo status del messaggio è "ACCEPTED"
    And il requestId nella risposta corrisponde a quello inviato
    And il cxId nella risposta corrisponde a quello inviato
    Examples:
      | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_accepted @invioIO_paymentData
  Scenario Outline: Invio messaggio IO con paymentData — risposta 200 ACCEPTED
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    And la richiesta include paymentData con amount 1500, noticeCode "302000100440009424" e creditorTaxId "01234567890"
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    And lo status del messaggio è "ACCEPTED"
    And il requestId nella risposta corrisponde a quello inviato
    And il cxId nella risposta corrisponde a quello inviato
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_accepted @invioIO_paymentData @invioIO_dueDate
  Scenario Outline: Invio messaggio IO con paymentData e dueDate — risposta 200 ACCEPTED
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    And la richiesta include dueDate "2026-12-31T23:59:59Z"
    And la richiesta include paymentData con amount 1500, noticeCode "302000100440009424", creditorTaxId "01234567890" e invalidAfterDueDate "true"
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    And lo status del messaggio è "ACCEPTED"
    And il requestId nella risposta corrisponde a quello inviato
    And il cxId nella risposta corrisponde a quello inviato
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_accepted @invioIO_sensitiveContent
  Scenario Outline: Invio messaggio IO con contenuto sensibile — risposta 200 ACCEPTED
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    And la richiesta ha sensitiveContent true
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    And lo status del messaggio è "ACCEPTED"
    And il requestId nella risposta corrisponde a quello inviato
    And il cxId nella risposta corrisponde a quello inviato
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_accepted @invioIO_pollingMaxHours
  Scenario Outline: Invio messaggio IO con pollingMaxHours personalizzato — risposta 200 ACCEPTED
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    And la richiesta include pollingMaxHours 72
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    And lo status del messaggio è "ACCEPTED"
    And il requestId nella risposta corrisponde a quello inviato
    And il cxId nella risposta corrisponde a quello inviato
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_accepted @invioIO_attachments
  Scenario Outline: Invio messaggio IO con lista allegati — risposta 200 ACCEPTED
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    And la richiesta include allegati
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    And lo status del messaggio è "ACCEPTED"
    And il requestId nella risposta corrisponde a quello inviato
    And il cxId nella risposta corrisponde a quello inviato
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_idempotente
  Scenario Outline: Reinvio con stesso requestId e stesso payload — risposta 204 (idempotenza)
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    When reinvio lo stesso messaggio IO con lo stesso requestId
    Then la risposta HTTP ha status 204
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_conflict
  Scenario Outline: Reinvio con stesso requestId ma payload diverso — risposta 409 (conflitto)
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    When reinvio lo stesso requestId con subject diverso "Titolo modificato"
    Then la risposta HTTP ha status 409
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_conflict @invioIO_conflict_cxId
  Scenario Outline: Reinvio con stesso requestId ma cxId diverso — risposta 409 (conflitto)
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    When reinvio lo stesso requestId con cxId diverso "pn-delivery-push-DIFFERENT"
    Then la risposta HTTP ha status 409
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_accepted @invioIO_subject_max
  Scenario Outline: Invio messaggio IO con subject di esattamente 120 caratteri — risposta 200 ACCEPTED
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    And la richiesta ha subject di 120 caratteri
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    And lo status del messaggio è "ACCEPTED"
    And il requestId nella risposta corrisponde a quello inviato
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_attachments_validation_failed @wip
  Scenario Outline: Invio messaggio IO con allegati non PDF — accettazione 200, poi ATTACHMENTS_VALIDATION_FAILED (asincrono)
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    And la richiesta include allegati non validi non PDF
    When invio il messaggio IO
    Then la risposta HTTP ha status 200
    And lo status del messaggio è "ACCEPTED"
    And attendo che lo status del messaggio diventi "ATTACHMENTS_VALIDATION_FAILED"
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_ko @invioIO_ko_campo_mancante
  Scenario Outline: Invio messaggio IO senza campo obbligatorio <campo> — risposta 400
    Given un messaggio IO senza il campo "<campo>"
    When invio il messaggio IO
    Then la risposta HTTP ha status 400
    Examples:
      | campo           |
      | requestId       |
      | subject         |
      | recipientTaxId  |
      | senderServiceId |
      | markdown        |

  @invioIO @postMessage @invioIO_ko @invioIO_ko_subject_lungo
  Scenario Outline: Invio messaggio IO con subject superiore a 120 caratteri — risposta 400
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    And la richiesta ha subject di 121 caratteri
    When invio il messaggio IO
    Then la risposta HTTP ha status 400
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |

  @invioIO @postMessage @invioIO_ko @invioIO_ko_header_mancante
  Scenario Outline: Invio messaggio IO senza header obbligatorio x-pagopa-iocon-cx-id — risposta 400
    Given un messaggio IO valido con iun "<iun>", recipientTaxId "<recipientTaxId>", senderServiceId "<senderServiceId>", subject "<subject>", markdown "<markdown>"
    When invio il messaggio IO senza l'header obbligatorio
    Then la risposta HTTP ha status 400
    Examples:
      | iun          | recipientTaxId       | senderServiceId       | subject             | markdown                                       |
      | @io.iun      | @io.recipientTaxId   | @io.senderServiceId   | Avviso di pagamento | Gentile cittadino, hai ricevuto un avviso. |
