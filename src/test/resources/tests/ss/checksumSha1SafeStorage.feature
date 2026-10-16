@PnSsChecksumSha1
Feature: Calcolo e salvataggio dello SHA-1 dei documenti SafeStorage

  @PN-21852
  Scenario Outline: Al termine del caricamento di un file non sottoposto a trasformazione viene salvato lo SHA-1 del contenuto
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss
    Then the document checkSumSha1 matches the SHA-1 of the uploaded file
    And the document checkSumSha1 matches the SHA-1 of the stored file

    Examples:
      | clientId           | APIKey            | documentType                       | fileName                    | MIMEType        |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.pdf | application/pdf |
      | @clientId-delivery | @delivery_api_key | @doc_type_notification_attachments | src/main/resources/test.zip | application/zip |

  @PN-21852 @Transformation
  Scenario Outline: Al termine della trasformazione viene salvato lo SHA-1 del file trasformato
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss
    Then the document checkSumSha1 matches the SHA-1 of the stored file

    Examples:
      | clientId       | APIKey       | documentType                     | fileName                    | MIMEType        |
      | @clientId-test | @apiKey_test | @doc_type_legal_facts            | src/main/resources/test.pdf | application/pdf |
      | @clientId-test | @apiKey_test | @doc_type_clean_paper_attachment | src/main/resources/test.pdf | application/pdf |

  @PN-21852
  Scenario: Lo SHA-1 resta interno e non viene esposto dalle API di SafeStorage
    Given "@clientId-delivery" authenticated by "@delivery_api_key" try to upload a document of type "@doc_type_notification_attachments" with content type "application/pdf" using "src/main/resources/test.pdf"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss
    Then the document checkSumSha1 matches the SHA-1 of the uploaded file
    And the document checkSumSha1 is not exposed by SafeStorage

  # Il client deve avere canExecutePatch (get-file-with-patch-configuration CLIENT_SPECIFIC): pn-test su dev.
  # Se la lambda elabora l'upload prima della getFile la gara non si riproduce e lo scenario risulta skipped.
  @PN-21852
  Scenario: Una getFile prima dell'elaborazione dell'upload non fa perdere lo SHA-1
    Given "@clientId-test" authenticated by "@apiKey_test" try to upload a document of type "@doc_type_notification_attachments" with content type "application/pdf" using "src/main/resources/test.pdf"
    When request a presigned url to upload the file
    And upload that file
    And the file is requested for download before SafeStorage processes the upload
    Then the checkSumSha1 of the uploaded file is saved when SafeStorage processes the upload
    And the document state and status change timestamp are unchanged

  @PN-21852
  Scenario: Un aggiornamento in ritardo non sovrascrive lo SHA-1 già salvato
    Given "@clientId-delivery" authenticated by "@delivery_api_key" try to upload a document of type "@doc_type_notification_attachments" with content type "application/pdf" using "src/main/resources/test.pdf"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss
    And an outdated patch with checkSumSha1 "qUqP5cyxm6YcTAhz05Hph5gvu9M=" is sent for the document
    Then the document checkSumSha1 matches the SHA-1 of the uploaded file
    And the outdated patch did not change the document
