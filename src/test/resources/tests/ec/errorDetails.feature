@PnEcSendMessage @errorDetails
Feature: Dettaglio errore interno (errorDetails) negli eventi di pn-EcRichiesteMetadati

  @invioPEC @complete_pec_ko
  Scenario Outline: Invio digitale ad un indirizzo PEC non valido e verifica di errorDetails
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message to "<receiver>"
    Then wait for the request to have event code error "<rc>"
    And the stored digital event with status "<status>" has a sanitized errorDetails not containing "<receiver>"
    And the external events of the request do not contain errorDetails
    Examples:
      | clientId           | channel      | receiver                 | rc   | status       |
      | @clientId-delivery | @channel_pec | .mario.rossi@arubapec.it | C011 | addressError |

  @PAPER @invioCartaceo @raster @TransformationError
  Scenario Outline: Invio di un messaggio cartaceo con allegato non valido e verifica di errorDetails
    Given a "<clientId>" and "<channel>" to send on
    When "@clientId-delivery" authenticated by "@delivery_api_key" uploads the following attachments:
      | documentType        | fileName                     | mimeType        |
      | @doc_type_to_raster | src/main/resources/wrong.pdf | application/pdf |
    And try to send a paper message to "<receiver>" with "<transformationDocumentType>" as documentType and "<paId>" as PaId
    * waiting for scheduling
    Then wait for the request to have status "<status>"
    And the stored paper event with statusCode "<status>" has errorDetails containing "Transformation error for key" and "documentStatus"
    And the errorDetails of the stored paper event with statusCode "<status>" do not contain the following values:
      | Paolo Rossi |
      | Bianchi |
      | @paper.receiver.digital.address |
      | scala A interno 4 |
      | 00017 |
      | MYYNA0JJART56HOZ |
    And the external events of the request do not contain errorDetails
    And the paper pull response of the request does not contain errorDetails
    Examples:
      | clientId       | channel        | receiver                        | transformationDocumentType | paId                       | status |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | @doc_type_paper_attachment | @paid_none_transformations | P013   |
