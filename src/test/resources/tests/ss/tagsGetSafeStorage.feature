@PnSsTagsGet
Feature: Get SafeStorage tags

  @PN-20716
  Scenario: Recupero dei tag di un documento con client autorizzato
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@tag" value "get-tag-value-" as "docGetTag"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When get the tags of document alias "docGetTag"
    Then i get an error "200"
    And the document tags contain "@tag" with value from alias "docGetTag"

  @PN-20716
  Scenario: Recupero dei tag di un documento senza tag restituisce una mappa vuota
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" as "docGetNoTags"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When get the tags of document alias "docGetNoTags"
    Then i get an error "200"
    And the document tags are empty

  @PN-20716
  Scenario: Recupero dei tag con client non autorizzato in lettura restituisce un errore di autorizzazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@tag" value "get-forbidden-value-" as "docGetForbidden"
    Given the SafeStorage client "@clientId-cons" authenticated by "@apiKey-cons"
    When get the tags of document alias "docGetForbidden"
    Then i get an error "403"

  @PN-20716
  Scenario: Recupero dei tag su una fileKey inesistente restituisce un errore di risorsa non trovata
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When get the tags of a non existing fileKey expecting failure
    Then i get an error "404"

  @PN-20716 @localTag
  Scenario: Recupero dei tag espone il tag locale senza il prefisso
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@indexedLocalTag" value "get-local-value-" as "docGetLocalTag"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When get the tags of document alias "docGetLocalTag"
    Then i get an error "200"
    And the document tags key "@indexedLocalTag" has no local prefix
