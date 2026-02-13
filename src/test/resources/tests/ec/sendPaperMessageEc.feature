Feature: Send Paper Message Ec

  @PnEcSendMessage @PAPER @invioCartaceo @testOk
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When "@clientId-delivery-push" authenticated by "@apiKey-delivery-push" uploads the following attachments:
      | documentType  | fileName                    | mimeType        |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf |
    When try to send a paper message to "<receiver>"
    * waiting for scheduling
    Then check if the message has been sent
    Examples:
      | clientId       | channel        | receiver                        |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address |

  @PnEcSendMessage @PAPER @invioCartaceo @raster @testOk
  Scenario Outline: Invio di un messaggio cartaceo con allegati da rasterizzare, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType        | fileName                           | mimeType        |
      | @doc_type_to_raster | src/test/resources/test-raster.pdf | application/pdf |
    And try to send a paper message to "<receiver>"
    When it's available
    # Bisogna aspettare 2 volte la schedulazione, la prima per lavorare la richiesta con gli allegati ancora da convertire,
    # la seconda per lavorare la richiesta con gli allegati ormai rasterizzati.
    * waiting for scheduling
    * waiting for scheduling
    Then check if the message has been sent
    Examples:
      | clientId           | apiKey            | channel        | receiver                        |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address |

  @PnEcSendMessage @PAPER @invioCartaceo @raster @testFlagRasterization
  Scenario Outline: Invio di un messaggio cartaceo con allegati da rasterizzare, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento per soli flag attivi
    Given a "<clientId>" and "<channel>" to send on
    When "@clientId-delivery" authenticated by "@delivery_api_key" uploads the following attachments:
      | documentType        | fileName                           | mimeType        |
      | @doc_type_to_raster | src/test/resources/test-raster.pdf | application/pdf |
    When try to send a paper message to "<receiver>" with "<requestPaId>" and "<applyRasterization>"
    # Bisogna aspettare 2 volte la schedulazione, la prima per lavorare la richiesta con gli allegati ancora da convertire,
    # la seconda per lavorare la richiesta con gli allegati ormai rasterizzati.
    * waiting for scheduling
    * waiting for scheduling
    Then check if the message has been sent
    # I casi con applyRasterization=true oppure con requestPaId note sono quelli in cui avviene la rasterizzazione:
    # primo caso in cui non esiste la pa ed il flag è false (nessuna conversione);
    # secondo caso in cui non esiste la paId ed il flag è true (nessuna conversione);
    # terzo caso in cui non esiste la paId ed il flag non è impostato (nessuna conversione);
    # quarto caso in cui esiste la paId ed il flag è impostato a true (conversione);
    # quinto caso in cui esiste la paId ed il flag è impostato a false (conversione);
    # quinto caso in cui esiste la paId ed il flag non è impostato (conversione);
    Examples:
      | clientId       | channel        | receiver                        | requestPaId  | applyRasterization |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | 19289210     | false              |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | requestPaId2 | true               |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | requestPaId2 |                    |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | 15376371009  | true               |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | 15376371009  | false              |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | 15376371009  |                    |

  @PnEcSendMessage @PAPER @invioCartaceo @testKo
  Scenario Outline: Invio di un messaggio cartaceo con clientId non valido e verifica dello statusCode
    Given a "<clientId>" and "<channel>" to send on
    When try to send a paper message to "<receiver>"
    Then I get "<rc>" status code
    Examples:
      | clientId     | channel        | rc  | receiver |
      | FakeClientId | @channel_paper | 403 | @paper.receiver.digital.address |


  @PnEcSendMessage @PAPER @invioCartaceo @raster @testOk @TransformationDocumentType @TransformationError
  Scenario Outline: Invio di un messaggio cartaceo con allegato vuoto e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType        | fileName                     | mimeType        |
      | @doc_type_to_raster | src/test/resources/empty.pdf | application/pdf |
    And try to send a paper message to "<receiver>" with "<transformationDocumentType>" as documentType
    # Attesa della schedulazione
    * waiting for scheduling
    Then check if the message has status "<status>"
    Examples:
      | clientId           | apiKey            | channel        | receiver                        | transformationDocumentType       | status |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | @doc_type_paper_attachment       | P013   |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | @doc_type_clean_paper_attachment | P013   |


  @PnEcSendMessage @PAPER @invioCartaceo @raster @testOk @TransformationDocumentType @TransformationError @P000
  Scenario Outline: Invio di un messaggio cartaceo con allegato valido e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType        | fileName                     | mimeType        |
      | @doc_type_to_raster | src/test/resources/test.pdf | application/pdf |
    And try to send a paper message to "<receiver>" with "<transformationDocumentType>" as documentType
    # Attesa della schedulazione
    * waiting for scheduling
    * waiting for scheduling
    Then check if the message has status "<status>"
    Examples:
      | clientId           | apiKey            | channel        | receiver                        | transformationDocumentType       | status |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | @doc_type_paper_attachment       | P000   |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | @doc_type_clean_paper_attachment | P000   |


  @PnEcSendMessage @PAPER @invioCartaceo @raster @testOk @TransformationDocumentType @TransformationError @P000
  Scenario Outline: Invio di un messaggio cartaceo con allegato valido e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When "@clientId-delivery" authenticated by "@delivery_api_key" uploads the following attachments:
      | documentType        | fileName                     | mimeType        |
      | @doc_type_to_raster | src/test/resources/test.pdf | application/pdf |
    And try to send a paper message to "<receiver>" with "<transformationDocumentType>" as documentType and "<paId>" as PaId
    # Attesa della schedulazione
    * waiting for scheduling
    * waiting for scheduling
    Then check if the message has status "<status>"
    Examples:
      | clientId       | channel        | receiver                        | transformationDocumentType       | paId                       | status |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | @doc_type_paper_attachment       | @paid_none_transformations | P000   |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | @doc_type_clean_paper_attachment | @paid_none_transformations | P000   |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | @doc_type_clean_paper_attachment | @paid_rasterization        | P000   |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address | @doc_type_paper_attachment       | @paid_normalization        | P000   |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address |                                  | @paid_none_transformations | P000   |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address |                                  | @paid_both_transformations | P000   |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address |                                  | @paid_rasterization        | P000   |
      | @clientId-cons | @channel_paper | @paper.receiver.digital.address |                                  | @paid_normalization        | P000   |