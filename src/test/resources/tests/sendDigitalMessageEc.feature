Feature: Send Digital Message Ec

  @invioSMS
  Scenario Outline: Invio sms e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId    |  channel |
      | pn-delivery | SMS      |
  ##    | pn-delivery | EMAIL    |
  ##    | pn-delivery | PEC      |

  Scenario Outline: Invio sms e verifica della pubblicazione del messaggio nella coda di errore
    Given try to send a sms with a "<clientId>"
    Then pubblicazione in coda "Errori SMS"
    Examples:
      | clientId |
      | pn-delivery |

  @invioEMAIL
  Scenario Outline: Invio email e verifica della pubblicazione del messaggio nella coda di debug
    Given a "<clientId>" and "<channel>" to send on
    When try to send a digital message
    Then check if the message has been sent
    Examples:
      | clientId    |  channel |
      | pn-delivery |   EMAIL  |
