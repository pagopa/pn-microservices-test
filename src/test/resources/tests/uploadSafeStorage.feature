@UploadSafeStorage
Feature: Upload SafeStorage


  Scenario Outline: Upload di un file non sottoposto a trasformazione con un clientId non riconosciuto
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    Then i get an error "<rc>"
    Examples:
      | clientId          | APIKey            | documentType                       | fileName      | MIMEType       | rc  |
      | @clientId-unknown | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.zip | application/zip | 403 |


  Scenario Outline: Upload di un file non sottoposto a trasformazione e verifica del messaggio di disponibilità del file
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    Then i found in S3
    And i check availability message "<rc>"
    Examples:
      | clientId           | APIKey            | documentType                       | fileName      | MIMEType       | rc |
     | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments| src/main/resources/test.zip | application/zip | 200 |
     | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments| src/main/resources/test.pdf | application/pdf | 200 |


  Scenario Outline: Casi di errore in fase di richiesta della presigned URL di upload
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    Then i get an error "<rc>"
    Examples:
      | clientId           | APIKey            | documentType   | fileName      | MIMEType       | rc  |
      | @clientId-delivery | @delivery_api_key | PN_LEGAL_FACTS | src/main/resources/test.zip | application/zip | 403 |


  Scenario Outline: update dei metadata di un file - cambio status o retentionUntil
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document just uploaded using "<status>" and "<retentionUntil>"
    Then i check that the document got updated
    Examples:
      | clientId           | APIKey            | documentType                       | fileName                    | MIMEType        | clientIdUp         | APIKeyUp          | status   | retentionUntil           |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-delivery | @delivery_api_key | ATTACHED |                          |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-delivery | @delivery_api_key | ATTACHED | 2024-05-04T16:15:00.000Z |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-delivery | @delivery_api_key |          | 2024-07-11T13:02:25.206Z |


  Scenario Outline: tentativo di update dei metadata di un file con chiave invalida o non valorizzata
    Given "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document using "<status>" and "<retentionUntil>" but has invalid or null "<fileKey>"
    Then i get an error "<rc>"
    Examples:
      | clientIdUp      | APIKeyUp      | status   | retentionUntil           | fileKey     | rc  |
      | @clientId-test | @apiKey_test| ATTACHED | 2024-07-11T13:02:25.206Z | NONEXISTENT | 404 |
      | @clientId-test | @apiKey_test | ATTACHED | 2024-07-11T13:02:25.206Z |             | 400 |


  Scenario Outline: tentativo di update dei metadata di un file con client non autorizzato o con status non valido/congruo
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document just uploaded using "<status>" and "<retentionUntil>"
    Then i get an error "<rc>"
    Examples:
      | clientId           | APIKey            | documentType                       | fileName      | MIMEType       | clientIdUp      | APIKeyUp      | status   | retentionUntil           | rc  |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-pn-cn | @apiKey-pn_cn | ATTACHED | 2024-07-11T13:02:25.206Z | 403 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf| application/pdf | @clientId-test  | @apiKey_test  | SAVED    | 2024-07-11T13:02:25.206Z | 400 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf | @clientId-test  | @apiKey_test  | NONEXIST | 2024-07-11T13:02:25.206Z | 400 |


  Scenario Outline: Upload di un file da sottoporre a trasformazione e verifica del messaggio di disponibilità del file
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    Then i found in S3
    And i check availability message "<rc>"
    Examples:
      | clientId       | APIKey       | documentType          | fileName      | MIMEType       | rc |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts | src/main/resources/test.zip | application/zip | 200 |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts | src/main/resources/test.pdf | application/pdf | 200 |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts | src/main/resources/test.xml | application/xml | 200 |