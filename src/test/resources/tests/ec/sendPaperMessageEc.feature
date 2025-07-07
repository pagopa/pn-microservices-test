Feature: Send Paper Message Ec


  @PnEcSendMessage @PAPER @invioCartaceo @raster @testOk @TransformationDocumentType @TestThis
  Scenario Outline: Invio di un messaggio cartaceo con allegati da rasterizzare, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento.
    Given a "<clientId>" and "<channel>" to send on
    When "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType        | fileName                     | mimeType        |
      | @doc_type_to_raster | src/test/resources/empty.pdf | application/pdf |
    And try to send a paper message to "<receiver>" with "<transformationDocumentType>" as documentType
    # Bisogna aspettare 2 volte la schedulazione, la prima per lavorare la richiesta con gli allegati ancora da convertire,
    # la seconda per lavorare la richiesta con gli allegati ormai rasterizzati.
    * waiting for scheduling
    Then check if the message has status "<status>"
    Examples:
      | clientId           | apiKey            | channel        | receiver                        | transformationDocumentType       | status |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | @doc_type_paper_attachment       | P013   |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | @doc_type_clean_paper_attachment | P013   |

