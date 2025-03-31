Feature: Send Paper Message Ec

  @PnEcSendMessage @PAPER @invioCartaceo @testOk
  Scenario Outline: Invio di un messaggio cartaceo, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType  | fileName                    | mimeType        |
      | @doc_type_aar | src/test/resources/test.pdf | application/pdf |
    When try to send a paper message to "<receiver>"
    * waiting for scheduling
    Then check if the message has been sent
    Examples:
      | clientId                | apiKey                | channel        | receiver                        |
      | @clientId-delivery-push | @apiKey-delivery-push | @channel_paper | @paper.receiver.digital.address |

  @PnEcSendMessage @PAPER @invioCartaceo @raster @testOk
  Scenario Outline: Invio di un messaggio cartaceo con allegati da rasterizzare, verifica della pubblicazione del messaggio nella coda di debug e verifica dello stato di avanzamento
    Given a "<clientId>" and "<channel>" to send on
    When "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
      | documentType        | fileName                           | mimeType        |
      | @doc_type_to_raster | src/test/resources/test-raster.pdf | application/pdf |
    And try to send a paper message to "<receiver>"
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
    When "<clientId>" authenticated by "<apiKey>" uploads the following attachments:
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
      | clientId           | apiKey            | channel        | receiver                        | requestPaId     | applyRasterization |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | 19289210        |       false        |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | requestPaId2    |       true         |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | requestPaId2    |                    |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | 15376371009     |       true         |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | 15376371009     |       false        |
      | @clientId-delivery | @delivery_api_key | @channel_paper | @paper.receiver.digital.address | 15376371009     |                    |

  @PnEcSendMessage @PAPER @invioCartaceo @testKo
  Scenario Outline: Invio di un messaggio cartaceo con clientId non valido e verifica dello statusCode
    Given a "<clientId>" and "<channel>" to send on
    When try to send a paper message to "<receiver>"
    Then I get "<rc>" status code
    Examples:
      | clientId     | channel        | rc  | receiver |
      | FakeClientId | @channel_paper | 403 | @paper.receiver.digital.address |


  # I test seguenti sono attualmente validi solo in localdev. Utilizzano una configurazione mockata del consolidatore,
  # tramite mockserver (vedi https://github.com/pagopa/pn-localdev/commit/0ad3976bb25d1d91559ec77690c1621aa012c477).
  # La stringa alla fine del receiver indica il comportamento che si vuole ottenere dal mockserver.
  # Per eseguirli, rimuovere temporaneamente il tag @ignore.

  @PnEcSendMessage @PAPER @invioCartaceo @ignore
  Scenario Outline: Invio di un messaggio cartaceo, verifica di inoltro progresso alla piattaforma esterna
    Given a "<clientId>" and "<channel>" to send on
    When try to send a paper message to "<receiver>"
    * waiting for scheduling
    Then check if the message has "<expectedStatus>" status code
    Examples:
      | clientId       | channel        | receiver                | expectedStatus |
      | @clientId-cons | @channel_paper | Via Roma                | P000           |
      | @clientId-cons | @channel_paper | Via Roma @syntaxError   | P011           |
      | @clientId-cons | @channel_paper | Via Roma @semanticError | P012           |

  @PnEcSendMessage @PAPER @invioCartaceo @ignore
  Scenario Outline: Invio di un messaggio cartaceo, verifica di cambio stato del documento
    Given a "<clientId>" and "<channel>" to send on
    When try to send a paper message to "<receiver>"
    * waiting for scheduling
    Then check if the request is in "<requestStatus>" state
    Examples:
      | clientId       | channel        | receiver                           | requestStatus       |
      | @clientId-cons | @channel_paper | Via Roma @duplicatedRequest        | duplicatedRequest   |
      | @clientId-cons | @channel_paper | Via Roma @authenticationError      | authenticationError |
      | @clientId-cons | @channel_paper | Via Roma @400_unrecognized_payload | error               |
      # La richiesta entra nel ciclo di retry.
      | @clientId-cons | @channel_paper | Via Roma @500_unrecognized_payload | retry               |
      | @clientId-cons | @channel_paper | Via Roma @internalError            | retry               |