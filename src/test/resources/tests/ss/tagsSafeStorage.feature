@PnSsTags
Feature: Search SafeStorage tags

  @PN-20716
  Scenario: Ricerca di un singolo tag globale trova la fileKey indicizzata
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@tag" value "search-single-value-" as "docSingleTag"
    Given the SafeStorage client "@clientId-pn-cn" authenticated by "@apiKey-pn_cn"
    When search files using tag "@tag" value from "docSingleTag"
    Then i get an error "200"
    And the search response contains fileKey alias "docSingleTag"

  @PN-20716
  Scenario: Ricerca AND di default con due tag entrambi presenti sullo stesso file trova la fileKey
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tags as "docAndTags"
      | tag               | value               |
      | @tag              | search-and-global-  |
      | @indexedLocalTag  | search-and-local-   |
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When search files using tags "@tag,@indexedLocalTag" from "docAndTags"
    Then i get an error "200"
    And the search response contains fileKey alias "docAndTags"

  @PN-20716
  Scenario: Ricerca AND di default con un tag non corrispondente restituisce un risultato vuoto
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@tag" value "search-and-nomatch-" as "docAndNoMatch"
    Given the SafeStorage client "@clientId-pn-cn" authenticated by "@apiKey-pn_cn"
    When search files using tag "@tag" value from "docAndNoMatch" and non matching tag "searchNoMatchTag"
    Then i get an error "200"
    And the search response is empty

  @PN-20716
  Scenario: Ricerca OR con due tag di cui uno corrispondente trova la fileKey
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@tag" value "search-or-value-" as "docOrTag"
    Given the SafeStorage client "@clientId-pn-cn" authenticated by "@apiKey-pn_cn"
    When search files using tag "@tag" value from "docOrTag" and non matching tag "searchOrNoMatchTag" with logic "or"
    Then i get an error "200"
    And the search response contains fileKey alias "docOrTag"

  @PN-20716 @localTag
  Scenario: Ricerca con tags=true espone i tag associati senza il prefisso locale
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@indexedLocalTag" value "search-tags-response-" as "docTagsResponse"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When search files using tag "@indexedLocalTag" value from "docTagsResponse" including tags in response
    Then i get an error "200"
    And the search response fileKey alias "docTagsResponse" has tags without local prefix

  @PN-20716 @localTag
  Scenario: Ricerca con tag locale non prefissato viene risolta lato server
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@indexedLocalTag" value "search-local-value-" as "docLocalTag"
    Given the SafeStorage client "@clientId-test" authenticated by "@apiKey_test"
    When search files using tag "@indexedLocalTag" value from "docLocalTag"
    Then i get an error "200"
    And the search response contains fileKey alias "docLocalTag"

  @PN-20716
  Scenario: Ricerca con numero di tag param oltre il limite MaxMapValuesForSearch restituisce un errore di validazione
    Given the SafeStorage client "@clientId-pn-cn" authenticated by "@apiKey-pn_cn"
    When search files with 11 tag params
    Then i get an error "400"

  @PN-20716
  Scenario: Ricerca senza alcun parametro tag restituisce un errore di validazione
    Given the SafeStorage client "@clientId-pn-cn" authenticated by "@apiKey-pn_cn"
    When search files with no tag params
    Then i get an error "400"

  @PN-20716
  Scenario: Ricerca con client non autorizzato in lettura sui tag restituisce un errore di autorizzazione
    Given "@clientId-test" authenticated by "@apiKey_test" upload and index a document of type "@doc_type_notification_attachments" with tag "@tag" value "search-forbidden-value-" as "docForbidden"
    Given the SafeStorage client "@clientId-cons" authenticated by "@apiKey-cons"
    When search files using tag "@tag" value from "docForbidden"
    Then i get an error "403"
