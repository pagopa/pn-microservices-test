Feature: Send Digital Message Ec

  @invioSMS
  Scenario Outline: Invio sms e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId    |  channel |
      | @clientId-delivery | @channel_sms |



    @invioPEC
      Scenario Outline: Invio pec e verifica della pubblicazione del messaggio nella coda di debug
      Given a "<clientId>" and "<channel>" to send on
      When try to send a digital message
      Then check if the message has been sent
      Examples:
        | clientId    |  channel |
        | @clientId-delivery | @channel_pec |

  @invioEMAIL
  Scenario Outline: Invio email e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId    |  channel |
      | @clientId-delivery |   @channel_email  |

  @invioCartaceo
  Scenario Outline: Invio di un messaggio cartaceo e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a paper message
    And check if the message has been sent
    Then check ricezione esiti
    Examples:
      | clientId    | channel |
      | @clientId-delivery | CARTACEO |


@complete_pec
  Scenario Outline: Invio pec e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    And "<clientIdSafeStorage>" authenticated by "<APIKey>" try to upload a document of type "<documentType>" with content type "<MIMEType>" using "<fileName>"
    When request a presigned url to upload the file
    And upload that file
    And it's available
    Then check if the message has been sent
    Then i check availability message

    Examples:
      | clientId    |  channel | clientIdSafeStorage | APIKey       | documentType          | fileName      | MIMEType       |
      | @clientId-delivery | @channel_pec | @clientId-test | @apiKey_test | @doc_type_legal_facts | src/main/resources/test.pdf | application/pdf |
