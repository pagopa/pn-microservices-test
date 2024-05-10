@PnEcSend
Feature: Send Digital Message Ec

  @invioSMS
  Scenario Outline: Invio sms e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId           | channel      |
      | @clientId-delivery | @channel_sms |


  @invioPEC
  Scenario Outline: Invio pec e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId           | channel      |
      | @clientId-delivery | @channel_pec |

  @invioEMAIL
  Scenario Outline: Invio email e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId           | channel        |
      | @clientId-delivery | @channel_email |

  @invioCartaceo
  Scenario Outline: Invio di un messaggio cartaceo e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a paper message
    And waiting for scheduling
    Then check if the message has been sent
    Examples:
      | clientId       | channel |
      | @clientId-cons | @channel_paper   |


  @complete_pec
  Scenario Outline: Invio pec e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message
    And check if the message has been sent
    And waiting for scheduling
    Then check if the message has been accepted and has been delivered


    Examples:
      | clientId           | channel      |
      | @clientId-delivery | @channel_pec |


  @complete_mail
  Scenario Outline: invio email e verifica della pubblicszione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    And I upload the following attachments:
      | documentType                       | fileName                    | mimeType        |
      | @doc_type_notification_attachments | src/test/resources/test.pdf | application/pdf |
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId           | channel        |
      | @clientId-delivery | @channel_email |