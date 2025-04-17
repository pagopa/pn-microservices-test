Feature: Validate Status stateMachine Manager

  @PnStateMachineValidateStatus @getClient @getProcess @getStatus @getNextStatus
  Scenario Outline: Valida Stato tramite chiamata GET all'endpoint
    Given a "<clientId>"
    When try to validate "<status>" of a "<process>" with "<nextStatus>"
    Then i get response if nextStatus is "<allowed>"
    Examples:

    #EMAIL
      | clientId           | process | status       | nextStatus   | allowed |
      | @clientId-delivery | EMAIL   | _any_        | internalError| true    |
      | @clientId-delivery | EMAIL   | _start_      | booked       | true    |
      | @clientId-delivery | EMAIL   | booked       | sent         | true    |
      | @clientId-delivery | EMAIL   | retry        | error        | true    |
      | @clientId-delivery | EMAIL   | retry        | sent         | true    |
    #  | @clientId-delivery | EMAIL   | sent         | _end_        | true  | impossibile passare a stato _end_
    #  | @clientId-delivery | EMAIL   | internalError| _end_        | true  | impossibile passare a stato _end_
    # TEST EMAIL ERROR
      | @clientId-delivery | EMAIL   | _start_       | sent         | false   |

    #PEC
      | @clientId-delivery | PEC     | _start_       | booked       | true   |
      | @clientId-delivery | PEC     | booked        | addressError | true   |
      | @clientId-delivery | PEC     | booked        | retry        | true   |
      | @clientId-delivery | PEC     | booked        | sent         | true   |
      | @clientId-delivery | PEC     | retry         | error        | true   |
      | @clientId-delivery | PEC     | sent          | accepted     | true   |
      | @clientId-delivery | PEC     | sent          | infected     | true   |
      | @clientId-delivery | PEC     | sent          | notAccepted  | true   |
      | @clientId-delivery | PEC     | accepted      | notDelivered | true   |
      | @clientId-delivery | PEC     | accepted      | deliveryWarn | true   |
      | @clientId-delivery | PEC     | accepted      | delivered    | true   |
      | @clientId-delivery | PEC     | deliveryWarn  | delivered    | true   |
    #TEST PEC ERROR
      | @clientId-delivery | PEC     | _start_       | delivered    | false  |

    #CARTACEO
      | @clientId-delivery | PAPER   | _start_      | booked             | true  |
      | @clientId-delivery | PAPER   | booked       | syntaxError        | true  |
      | @clientId-delivery | PAPER   | booked       | semanticError      | true  |
      | @clientId-delivery | PAPER   | booked       | authenticationError| true  |
      | @clientId-delivery | PAPER   | booked       | duplicatedRequest  | true  |
      | @clientId-delivery | PAPER   | booked       | retry              | true  |
      | @clientId-delivery | PAPER   | booked       | sent               | true  |
      | @clientId-delivery | PAPER   | sent         | _any_              | true  |
      | @clientId-delivery | PAPER   | retry        | error              | true  |
    #TEST ERROR CARTACEO
      | @clientId-delivery | PAPER   | _start_      | sent               | false |
    #TEST _ANY_TO_ANY CARTACE DA RIVEDERE????
      | @clientId-delivery | PAPER   | sent         | booked             | true  |
      | @clientId-delivery | PAPER   | sent         | _start_            | true  |

    #SERCQ
      | @clientId-delivery | SERCQ    | _start_     | booked             | true  |
      | @clientId-delivery | SERCQ    | booked      | addressError       | true  |
      | @clientId-delivery | SERCQ    | booked      | sent               | true  |
    #TEST ERROR SERCQ
      | @clientId-delivery | SERCQ    | _start_     | sent               | false |

    #SMS
      | @clientId-delivery | SMS      | _start_     | booked             | true  |
      | @clientId-delivery | SMS      | booked      | retry              | true  |
    # NON PRESENTE SU DYNAMO MA PRESENTE SUL DOCUMENTO SUPPONGO PERCHE' C'E' IL RETRY | @clientId-delivery | SMS      | booked      | error              | true  |
      | @clientId-delivery | SMS      | booked      | sent               | true  |
      | @clientId-delivery | SMS      | retry       | sent               | true  |
    #ERROR SMS
      | @clientId-delivery | SMS      | _start_     | sent               | false |




  @PnStateMachineValidateStatus @getClient @getProcess @getStatus @getNextStatus
  Scenario Outline: Valida external-status tramite chiamata GET all'endpoint
    Given a "<clientId>"
    When try to validate a "<status>" of a "<process>"
    Then i get "<externalStatus>" and "<logicStatus>"
    Examples:
      | clientId           | process | status  | externalStatus| logicStatus |
      | @clientId-delivery | EMAIL   | retry   | PROGRESS      | null        |
      | @clientId-delivery | EMAIL   | booked  | PROGRESS      | null        |
