Feature: Rasterizzazione asincrona di un documento caricato su SafeStorage

  @PnPdfRaster @Transformation
  Scenario Outline: Upload di un file da sottoporre a rasterizzazione e verifica del messaggio di disponibilità del file
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss
    Then i found in S3
    And i check availability message "<rc>"
    Examples:
      | clientId       | APIKey       | documentType               | fileName                    | MIMEType        | rc  |
      | @clientId-cons | @apiKey-cons | @doc_type_paper_attachment | src/main/resources/test.pdf | application/pdf | 200 |

  @PnPdfRaster @Transformation
  Scenario Outline: Upload di un file da sottoporre a rasterizzazione. La rasterizzazione fallisce permanentemente e viene verificato l'arrivo dell'evento di indisponibilità del file.
    Given "<clientId>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    Then i check unavailability message "<rc>"
    Examples:
      | clientId       | APIKey       | documentType               | fileName                     | MIMEType        | rc  |
      | @clientId-test | @apiKey_test | @doc_type_paper_attachment | src/main/resources/wrong.pdf | application/pdf | 200 |
