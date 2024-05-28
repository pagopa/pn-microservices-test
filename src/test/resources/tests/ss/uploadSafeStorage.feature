Feature: Upload SafeStorage

  @PnSsUpload
  Scenario Outline: Upload di un file non sottoposto a trasformazione con un clientId non riconosciuto
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    Then i get an error "<rc>"
    Examples:
      | clientId          | APIKey            | documentType                       | fileName      | MIMEType       | rc  |
      | @clientId-unknown | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.zip | application/zip | 403 |

  @PnSsUpload
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

  @PnSsUpload
  Scenario Outline: Casi di errore in fase di richiesta della presigned URL di upload
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    Then i get an error "<rc>"
    Examples:
      | clientId           | APIKey            | documentType   | fileName      | MIMEType       | rc  |
      | @clientId-delivery | @delivery_api_key | PN_LEGAL_FACTS | src/main/resources/test.zip | application/zip | 403 |

  @PnSsUpload
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

  @PnSsUpload @getFile
    Scenario Outline: getFile di un file con una fileKey definita come parametro e metadataOnly
    Given "<clientId>" authenticated by "<APIKey>" try to get a file with key "<fileKey>" and metadataOnly as "false"
    When request a presigned url to download the file
    Then i get that presigned url
    Examples:
      | clientId | APIKey | fileKey |
      | @clientId-test | @apiKey_test | PN_EXTERNAL_LEGAL_FACTS-b5cb17897bcd4aadaa8e9784e8618c57.pdf |

  @PnSsUpload @getFile
  Scenario Outline: getFile di un file con una fileKey definita come parametro e metadataOnly
    Given "<clientId>" authenticated by "<APIKey>" try to get a file with key "<fileKey>" and metadataOnly as "true"
    When request a presigned url to download the file
    Then i get file metadata
    Examples:
      | clientId | APIKey | fileKey |
      | @clientId-test | @apiKey_test | PN_EXTERNAL_LEGAL_FACTS-b5cb17897bcd4aadaa8e9784e8618c57.pdf |

