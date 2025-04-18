Feature: PDF raster conversion via pn-pdfraster API

  Scenario Outline: Successful PDF conversion with various input files
    Given a valid PDF document named "<fileName>"
    When I send a multipart POST request to "/PDFRaster/convert"
    Then I should receive an HTTP response with status 200
    And the response should contain a valid rasterized PDF document
    Examples:
      | fileName    |
      | test.pdf    |

  Scenario Outline: PDF conversion with invalid input files
    Given an invalid PDF document named "<fileName>"
    When I send a multipart POST request to "/PDFRaster/convert"
    Then I should receive an HTTP response with status 400
    Examples:
      | fileName              |
      | empty.pdf             |

  Scenario Outline: PDF conversion causing internal server error
    Given an invalid PDF document named "<fileName>"
    When I send a multipart POST request to "/PDFRaster/convert"
    Then I should receive an HTTP response with status 500
    Examples:
      | fileName              |
      | corrupted-document.pdf|
