@PnSsTagsMassive
Feature: Massive update of SafeStorage tags

  @PN-20716
  Scenario: SET massivo di un tag globale su piu' documenti
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docMassiveSet1"
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docMassiveSet2"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When massive set tag "@tag" value "massive-set-value-" on documents "docMassiveSet1,docMassiveSet2"
    Then i get an error "200"
    And the massive response has 0 errors

  @PN-20716
  Scenario: DELETE massivo di un tag precedentemente settato
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@tag" value "massive-delete-value-" as "docMassiveDelete"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When massive delete tag "@tag" on documents "docMassiveDelete"
    Then i get an error "200"
    And the massive response has 0 errors

  @PN-20716
  Scenario: Richiesta massiva con fileKey duplicata restituisce un errore di validazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docMassiveDuplicate"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When massive request with duplicate file key alias "docMassiveDuplicate" expecting failure
    Then i get an error "400"

  @PN-20716
  Scenario: Richiesta massiva oltre il limite MaxFileKeysUpdateMassivePerRequest restituisce un errore di validazione
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When massive update with 101 file keys expecting failure
    Then i get an error "400"

  @PN-20716
  Scenario: Update massivo con client non autorizzato in scrittura sui tag restituisce un errore di autorizzazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docMassiveForbidden"
    Given the SafeStorage client "@clientId-cons" authenticated by "@apiKey-cons"
    When massive set tag "@tag" value "massive-forbidden-value-" on documents "docMassiveForbidden"
    Then i get an error "403"

  @PN-20716
  Scenario: Richiesta massiva con errori parziali restituisce 200 e l'errore per la sola fileKey non valida
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docMassivePartialValid"
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docMassivePartialInvalid"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When massive update tag "@tag" value "massive-partial-value-" on document alias "docMassivePartialValid" and invalid tag "nonExistentTagXYZ" on document alias "docMassivePartialInvalid" expecting partial errors
    Then i get an error "200"
    And the massive response has 1 errors
    And the massive response has an error for fileKey alias "docMassivePartialInvalid"

  @PN-20716 @localTag
  Scenario: SET massivo di un tag locale non prefissato viene risolto lato server e trovato in ricerca
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docMassiveLocalTag"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When massive set tag "@indexedLocalTag" value "massive-local-value-" on documents "docMassiveLocalTag"
    Then i get an error "200"
    When search files using tag "@indexedLocalTag" value from "docMassiveLocalTag"
    Then i get an error "200"
    And the search response contains fileKey alias "docMassiveLocalTag"
