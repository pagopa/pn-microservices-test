Feature: Upload SafeStorage
#CASE TEST KO
  #in cima perch� "it's available rimane in pending a causa della lambda che non cambia stato al file

  Scenario Outline: Upload di un file non sottoposto a trasformazione con un clientId non riconosciuto
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    Then i get an error "<rc>"
    Examples:
      | clientId          | APIKey            | documentType                       | fileName      | MIMEType       | rc  |
      | @clientId-unknown | @delivery_api_key | @doc_type_notification_attachments | @filename_zip | @mime_type_zip | 403 |


  Scenario Outline: Upload di un file da sottoporre a trasformazione con un clientId non riconosciuto
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    Then i get an error "<rc>"
    Examples:
      | clientId          | APIKey            | documentType                       | fileName      | MIMEType       | rc  |
      | @clientId-unknown | @delivery_api_key | @doc_type_notification_attachments | @filename_zip | @mime_type_zip | 403 |


# END TEST KO

  Scenario Outline: Upload di un file non sottoposto a trasformazione
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    Then i found in S3
    Examples:
      | clientId           | APIKey            | documentType                       | fileName      | MIMEType       |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | @filename_zip | @mime_type_zip |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | @filename_pdf | @mime_type_pdf |


  @check_message_in_queue
  Scenario Outline: Upload di un file non sottoposto a trasformazione e verifica del messaggio di disponibilità del file
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    Then i found in S3
    And i check availability message
    Examples:
      | clientId           | APIKey            | documentType                       | fileName      | MIMEType       |
     | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments| @filename_zip | @mime_type_zip |
     | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments| @filename_pdf | @mime_type_pdf |

  Scenario Outline: Casi di errore in fase di richiesta della presigned URL di upload
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    Then i get an error "<rc>"
    Examples:
      | clientId           | APIKey            | documentType   | fileName      | MIMEType       | rc  |
      | @clientId-delivery | @delivery_api_key | PN_LEGAL_FACTS | @filename_zip | @mime_type_zip | 403 |

    # status change e status+date change non disponibili a causa della lambda, da verificare in ambiente corretto
  Scenario Outline: update dei metadata di un file - cambio status o retentionUntil
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document just uploaded using "<status>" and "<retentionUntil>"
    Then i check that the document got updated
    Examples:
      | clientId           | APIKey            | documentType                       | fileName      | MIMEType       | clientIdUp | APIKeyUp        | status   | retentionUntil           |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | @filename_pdf | @mime_type_pdf | pn-test    | @apiKey_test | ATTACHED | 2024-07-11T13:02:25.206Z |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | @filename_pdf | @mime_type_pdf | pn-test    | @apiKey_test | ATTACHED |                          |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | @filename_pdf | @mime_type_pdf | pn-test    | @apiKey_test |          | 2024-07-11T13:02:25.206Z |

  Scenario Outline: tentativo di update dei metadata di un file con chiave invalida o non valorizzata
    Given "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document using "<status>" and "<retentionUntil>" but has invalid or null "<fileKey>"
    Then i get an error "<rc>"
    Examples:
      | clientIdUp      | APIKeyUp      | status   | retentionUntil           | fileKey     | rc  |
      | @clientId-test | @apiKey_test| ATTACHED | 2024-07-11T13:02:25.206Z | NONEXISTENT | 404 |
      | pn-test | pn-test_api_key | ATTACHED | 2024-07-11T13:02:25.206Z |             | 400 |

  @upload_metadata
  Scenario Outline: tentativo di update dei metadata di un file con client non autorizzato o con status non valido/congruo
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    And "<clientIdUp>" authenticated by "<APIKeyUp>" try to update the document just uploaded using "<status>" and "<retentionUntil>"
    Then i get an error "<rc>"
    Examples:
      | clientId           | APIKey            | documentType                       | fileName      | MIMEType       | clientIdUp      | APIKeyUp      | status   | retentionUntil           | rc  |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | @filename_pdf | @mime_type_pdf | @clientId-pn-cn | @apiKey-pn_cn | ATTACHED | 2024-07-11T13:02:25.206Z | 403 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | @filename_pdf | @mime_type_pdf | @clientId-test  | @apiKey_test  | SAVED    | 2024-07-11T13:02:25.206Z | 400 |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | @filename_pdf | @mime_type_pdf | @clientId-test  | @apiKey_test  | NONEXIST | 2024-07-11T13:02:25.206Z | 400 |
   #   | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | @filename_pdf | @mime_type_pdf | @clientId-test  | @apiKey_test  | ATTACHED | 2022-07-11T13:02:25.206Z | 400 |

  @upload_trasformazione
  Scenario Outline: Upload di un file da sottoporre a trasformazione
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    Then i found in S3
    Examples:
      | clientId       | APIKey       | documentType          | fileName      | MIMEType       |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts | @filename_zip | @mime_type_zip |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts | @filename_pdf | @mime_type_pdf |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts | @filename_xml | @mime_type_xml |


  @upload_trasformazione
  Scenario Outline: Upload di un file da sottoporre a trasformazione e verifica del messaggio di disponibilità del file
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    Then i found in S3
    And i check availability message
    Examples:
      | clientId       | APIKey       | documentType          | fileName      | MIMEType       |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts | @filename_zip | @mime_type_zip |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts | @filename_pdf | @mime_type_pdf |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts | @filename_xml | @mime_type_xml |