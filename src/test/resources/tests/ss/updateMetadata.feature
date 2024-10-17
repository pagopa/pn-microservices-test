Feature: Update metadata

  @PnSsUpdateMetadata
  Scenario Outline: update dei metadata di un file - cambio status o retentionUntil
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document using "<status>" and "<retentionUntil>"
    Then i check that the document got updated
    And i check availability message "<rc>"
    Examples:
      | clientId           | APIKey            | documentType                       | fileName                    | MIMEType        | clientIdUp         | APIKeyUp          | status   | retentionUntil           | rc  |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-delivery | @delivery_api_key | ATTACHED |                          | 200 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-delivery | @delivery_api_key | ATTACHED | 2025-07-11T16:15:00.000Z | 200 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-delivery | @delivery_api_key |          | 2025-07-11T13:02:25.206Z | 200 |


  @PnSsUpdateMetadata
  Scenario Outline: tentativo di update dei metadata di un file con chiave invalida o non valorizzata
    Given "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document using "<status>" and "<retentionUntil>" but has invalid or null "<fileKey>"
    Then i get an error "<rc>"
    Examples:
      | clientIdUp     | APIKeyUp     | status   | retentionUntil           | fileKey     | rc  |
      | @clientId-test | @apiKey_test | ATTACHED | 2024-07-11T13:02:25.206Z | NONEXISTENT | 404 |
      | @clientId-test | @apiKey_test | ATTACHED | 2024-07-11T13:02:25.206Z |             | 400 |


  @PnSsUpdateMetadata
  Scenario Outline: tentativo di update dei metadata di un file con status non valido/congruo
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document using "<status>" and "<retentionUntil>"
    Then i get an error "<rc>"
    Examples:
      | clientId           | APIKey            | documentType                       | fileName                    | MIMEType        | clientIdUp      | APIKeyUp      | status   | retentionUntil           | rc  |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-test  | @apiKey_test  | SAVED    | 2025-07-11T13:02:25.206Z | 400 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-test  | @apiKey_test  | NONEXIST | 2025-07-11T13:02:25.206Z | 400 |

  @PnSsUpdateMetadata
  Scenario Outline: tentativo di update dei metadata di un file con client non autorizzato
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document using "<status>" and "<retentionUntil>"
    Then i get an error "<rc>"
    Examples:
      | clientId           | APIKey            | documentType                       | fileName                    | MIMEType        | clientIdUp      | APIKeyUp      | status   | retentionUntil           | rc  |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-pn-cn | @apiKey-pn_cn | ATTACHED | 2025-07-11T13:02:25.206Z | 403 |


  ### Casi con fileKey già valorizzate in input. ###

  @PnSsUpdateMetadata @updateFile @ignore
  Scenario Outline: update di un file con una fileKey definita e uno stato oppure una retentionUntil
    Given a document with fileKey "<fileKey>"
    When "<clientId>" authenticated by "<APIKey>" try to update the document using "<status>" and "<retentionUntil>"
    Then i check that the document got updated
    Examples:
      | clientId       | APIKey       | fileKey                                                          | status   | retentionUntil           |
      | @clientId-test | @apiKey_test | PN_NOTIFICATION_ATTACHMENTS-5d2ac4eff32b4ffa8a875305ff24a528.pdf | ATTACHED | 2025-07-11T13:02:25.206Z |
