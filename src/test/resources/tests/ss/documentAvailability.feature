@PnSsAvailability
Feature: Fine disponibilità dei documenti SafeStorage

  Background:
    Given "@clientId-delivery" authenticated by "@delivery_api_key" try to upload a document of type "@doc_type_notification_attachments" with content type "application/pdf" using "src/main/resources/test.pdf"
    When request a presigned url to upload the file
    And upload that file
    And it's available_ss

  @PN-20896
  Scenario: Aggiornamento senza data di disponibilità, il comportamento resta quello attuale
    When "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document using "ATTACHED", retentionUntil "today+30" and availableUntil ""
    Then i get an error "200"
    And i check that the document retention is still the one of "today+30"
    And i check that the document has no availability date

  @PN-20896
  Scenario: Una data di disponibilità già trascorsa viene rifiutata
    When "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document using availableUntil "yesterday"
    Then i get an error "400"
    And i check that the document has no availability date

  @PN-20896
  Scenario: La giornata corrente è ammessa
    When "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document using availableUntil "today"
    Then i get an error "200"
    And i check that the document availability is the end of the day of "today"

  @PN-20896
  Scenario: La data ricevuta viene normalizzata alla fine della giornata indicata
    When "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document using availableUntil "today+30"
    Then i get an error "200"
    And i check that the document availability is the end of the day of "today+30"

  @PN-20896
  Scenario: Una disponibilità oltre la retention della richiesta allunga la retention
    When "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document using "ATTACHED", retentionUntil "today+30" and availableUntil "today+60"
    Then i get an error "200"
    And i check that the document availability is the end of the day of "today+60"
    And i check that the document retention is the end of the day of "today+60"

  @PN-20896
  Scenario: Una disponibilità entro la retention della richiesta lascia la retention invariata
    When "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document using "ATTACHED", retentionUntil "today+60" and availableUntil "today+30"
    Then i get an error "200"
    And i check that the document availability is the end of the day of "today+30"
    And i check that the document retention is still the one of "today+60"

  @PN-20896
  Scenario: Tra più aggiornamenti prevale l'ultima data di disponibilità ricevuta
    When "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document using "ATTACHED", retentionUntil "today+30" and availableUntil "today+60"
    And "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document using availableUntil "today+90"
    Then i get an error "200"
    And i check that the document availability is the end of the day of "today+90"
    And i check that the document retention is the end of the day of "today+90"

  # Lo scenario seguente richiede un documento la cui disponibilità sia già trascorsa, condizione che non
  # è producibile nella run perché una data già trascorsa viene rifiutata: indicare una fileKey preparata.

  @PN-20896 @ignore
  Scenario: Oltre la fine della disponibilità la lettura del documento viene negata
    Given the SafeStorage client "@clientId-delivery" authenticated by "@delivery_api_key"
    And a document with fileKey "<insert fileKey>"
    Then reading the document is denied with "410" and a message about the end of availability

  @PN-20896
  Scenario: I metadati riportano come retention la data di fine disponibilità
    When "@clientId-delivery" authenticated by "@delivery_api_key" try to update the document using "ATTACHED", retentionUntil "today+60" and availableUntil "today+30"
    Then i get an error "200"
    And the file metadata response reports retentionUntil as the end of the day of "today+30"
