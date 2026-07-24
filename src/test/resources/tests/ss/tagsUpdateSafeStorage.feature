@PnSsTagsUpdate
Feature: Update SafeStorage tags

  @PN-20716
  Scenario: SET di un tag globale su un documento
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docSetTag"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When set tag "@tag" value "update-set-value-" on document alias "docSetTag"
    Then i get an error "200"

  @PN-20716
  Scenario: DELETE di un tag precedentemente settato
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@tag" value "update-delete-value-" as "docDeleteTag"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When delete tag "@tag" on document alias "docDeleteTag"
    Then i get an error "200"

  @PN-20716
  Scenario: SET e DELETE di tag diversi in un'unica richiesta
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@tag" value "update-mix-delete-value-" as "docMixTags"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When set tag "@indexedLocalTag" and delete tag "@tag" on document alias "docMixTags"
    Then i get an error "200"

  @PN-20716
  Scenario: SET e DELETE che referenziano la stessa chiave tag restituisce un errore di validazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docConflictTags"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When set and delete the same tag "@tag" on document alias "docConflictTags" expecting failure
    Then i get an error "400"

  @PN-20716
  Scenario: Update di un tag single-value con più valori restituisce un errore di validazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docSingleValueViolation"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When set tag "@indexedLocalTag" with 2 values on document alias "docSingleValueViolation" expecting failure
    Then i get an error "400"

  @PN-20716
  Scenario: Update con numero di operazioni oltre il limite MaxOperationsOnTagsPerRequest restituisce un errore di validazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docOperationsLimit"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When update with 51 operations on document alias "docOperationsLimit" expecting failure
    Then i get an error "400"

  @PN-20716
  Scenario: Update con numero di valori per tag oltre il limite MaxValuesPerTagPerRequest restituisce un errore di validazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docValuesLimit"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When set tag "@multiValueTag" with 101 values on document alias "docValuesLimit" expecting failure
    Then i get an error "400"

  @PN-20716
  Scenario: Update di un tag inesistente restituisce un errore di validazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docInvalidTag"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When set tag "nonExistentTagXYZ" value "invalid-tag-value-" on document alias "docInvalidTag" expecting failure
    Then i get an error "400"

  @PN-20716
  Scenario: Update con client non autorizzato in scrittura sui tag restituisce un errore di autorizzazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docForbiddenUpdate"
    Given the SafeStorage client "@clientId-cons" authenticated by "@apiKey-cons"
    When set tag "@tag" value "update-forbidden-value-" on document alias "docForbiddenUpdate"
    Then i get an error "403"

  @PN-20716
  Scenario: Update su una fileKey inesistente restituisce un errore di risorsa non trovata
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When update tag "@tag" on a non existing fileKey expecting failure
    Then i get an error "404"

  @PN-20716 @localTag
  Scenario: SET di un tag locale non prefissato viene risolto lato server e trovato in ricerca
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docLocalUpdate"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When set tag "@indexedLocalTag" value "update-local-transparency-" on document alias "docLocalUpdate"
    Then i get an error "200"
    When search files using tag "@indexedLocalTag" value from "docLocalUpdate"
    Then i get an error "200"
    And the search response contains fileKey alias "docLocalUpdate"

  @PN-20716 @localTag
  Scenario: DELETE di un tag locale non prefissato viene risolto lato server e non trovato in ricerca
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docLocalUpdateDelete"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When set tag "@indexedLocalTag" value "update-local-delete-" on document alias "docLocalUpdateDelete"
    Then i get an error "200"
    When delete tag "@indexedLocalTag" on document alias "docLocalUpdateDelete"
    Then i get an error "200"
    When search files using tag "@indexedLocalTag" value from "docLocalUpdateDelete"
    Then i get an error "200"
    And the search response is empty
