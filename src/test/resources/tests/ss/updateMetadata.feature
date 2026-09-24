Feature: Update metadata

  @PnSsUpdateMetadata
  Scenario Outline: update dei metadata di un file - cambio status o retentionUntil
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document with:
      | status         | <status>         |
      | retentionUntil | <retentionUntil> |
    Then i check that the document got updated
    And i check that the expiration registry reports "<retentionUntil>"
    And i check availability message "<rc>"
    Examples:
      | clientId           | APIKey            | documentType                       | fileName                    | MIMEType        | clientIdUp         | APIKeyUp          | status   | retentionUntil           | rc  |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-delivery | @delivery_api_key | ATTACHED |                          | 200 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-delivery | @delivery_api_key | ATTACHED | today+30                 | 200 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-delivery | @delivery_api_key |          | today+30                 | 200 |


  @PnSsUpdateMetadata
  Scenario Outline: tentativo di update dei metadata di un file con chiave inesistente
    Given "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document with:
      | fileKey        | <fileKey>        |
      | status         | <status>         |
      | retentionUntil | <retentionUntil> |
    Then i get the response status "<rc>"
    Examples:
      | clientIdUp     | APIKeyUp     | status   | retentionUntil           | fileKey     | rc  |
      | @clientId-test | @apiKey_test | ATTACHED | today+30                 | NONEXISTENT | 404 |


  @PnSsUpdateMetadata
  Scenario Outline: tentativo di update dei metadata di un file con status non valido/congruo
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document with:
      | status         | <status>         |
      | retentionUntil | <retentionUntil> |
    Then i get the response status "<rc>"
    Examples:
      | clientId           | APIKey            | documentType                       | fileName                    | MIMEType        | clientIdUp      | APIKeyUp      | status   | retentionUntil           | rc  |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-test  | @apiKey_test  | SAVED    | today+30                 | 400 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-test  | @apiKey_test  | NONEXIST | today+30                 | 400 |

  # TODO Lo scenario seguente e' escluso dalla run a causa di un bug. Il difetto e' tracciato da PN-21599: alla sua risoluzione il tag @ignore va rimosso.
  @PnSsUpdateMetadata @ignore
  Scenario: tentativo di update dei metadata di un file con una retention gia' trascorsa
    Given "@clientId-delivery" authenticated by "@delivery_api_key" try to upload a document of type "@doc_type_notification_attachments" with content type "application/pdf" using "src/main/resources/test.pdf"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss
    And "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document with:
      | retentionUntil | today-1 |
    Then i get the response status "400"
    And i check that the expiration registry reports ""

  @PnSsUpdateMetadata
  Scenario Outline: tentativo di update dei metadata di un file con client non autorizzato
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document with:
      | status         | <status>         |
      | retentionUntil | <retentionUntil> |
    Then i get the response status "<rc>"
    Examples:
      | clientId           | APIKey            | documentType                       | fileName                    | MIMEType        | clientIdUp      | APIKeyUp      | status   | retentionUntil           | rc  |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-pn-cn | @apiKey-pn_cn | ATTACHED | today+30                 | 403 |


  # I test seguenti sono specifici su risorse già esistenti a sistema. Per questo sono esclusi dalla run di test globale.

  @PnSsUpdateMetadata @updateFile @ignore
  Scenario Outline: update di un file con una fileKey definita e uno stato oppure una retentionUntil
    Given a document with fileKey "<fileKey>"
    When "<clientId>" authenticated by "<APIKey>" try to update the document with:
      | status         | <status>         |
      | retentionUntil | <retentionUntil> |
    Then i check that the document got updated
    Examples:
      | clientId       | APIKey       | fileKey          | status   | retentionUntil           |
      | @clientId-test | @apiKey_test | <insert fileKey> | ATTACHED | today+30                 |
