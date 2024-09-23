Feature: Download SafeStorage

  Background:
    Given "@clientId-delivery" authenticated by "@delivery_api_key" try to upload a document of type "@doc_type_notification_attachments" with content type "application/pdf" using "src/main/resources/test.pdf"
    When request a presigned url to upload the file
    And upload that file
    Then i found in S3
    And i check availability message "200"

  @PnSsDownload
  Scenario Outline: Richiesta di presignedUrl di download.
    Given the SafeStorage client "<clientId>" authenticated by "<APIKey>"
    When request a presigned url to download the file
    Then i get that presigned url
    Examples:
      | clientId       | APIKey       |
      | @clientId-test | @apiKey_test |

  @PnSsDownload @notAuthorized
  Scenario Outline: Richiesta di presignedUrl di download con client non autorizzato sul documentType
    Given the SafeStorage client "<clientId>" authenticated by "<APIKey>"
    When request a presigned url to download the file
    Then I get "<statusCode>" statusCode
    Examples:
      | clientId        | APIKey        | statusCode |
      | @clientId-pn-cn | @apiKey-pn_cn | 403        |


  ### Casi con fileKey già valorizzate in input. ###

  @PnSsDownload @getFile @ignore
  Scenario Outline: Richiesta di presignedUrl di download per una fileKey definita.
    Given "<clientId>" authenticated by "<APIKey>" try to get a file with key "<fileKey>" and metadataOnly as "false"
    When request a presigned url to download the file
    Then i get that presigned url
    Examples:
      | clientId       | APIKey       | fileKey                                                      |
      | @clientId-test | @apiKey_test | PN_EXTERNAL_LEGAL_FACTS-b5cb17897bcd4aadaa8e9784e8618c57.pdf |

  @PnSsDownload @getFile @ignore
  Scenario Outline: Richiesta dei metadata di un file con una fileKey definita.
    Given "<clientId>" authenticated by "<APIKey>" try to get a file with key "<fileKey>" and metadataOnly as "true"
    When request a presigned url to download the file
    Then i get file metadata
    Examples:
      | clientId       | APIKey       | fileKey                                                      |
      | @clientId-test | @apiKey_test | PN_EXTERNAL_LEGAL_FACTS-b5cb17897bcd4aadaa8e9784e8618c57.pdf |