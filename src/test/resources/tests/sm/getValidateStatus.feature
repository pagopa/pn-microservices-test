Feature: Validate Status stateMachine Manager

  @PnStateMachineValidateStatus @getClient @getProcess @getStatus @getNextStatus
  Scenario Outline: Valida Cambio Stato tramite chiamata GET all'endpoint
    Given a "<clientId>"
    When try to validate "<status>" of a "<process>" with "<nextStatus>"
    Then i get response if nextStatus is "<allowed>"
    Examples:

    #EMAIL
      | clientId           | process | status       | nextStatus   | allowed |
      | @clientId-delivery | EMAIL   | booked       | sent         | true    |
      | @clientId-delivery | EMAIL   | retry        | error        | true    |
      | @clientId-delivery | EMAIL   | retry        | sent         | true    |
    #  | @clientId-delivery | EMAIL   | sent         | _end_        | true  | impossibile passare a stato _end_
    #  | @clientId-delivery | EMAIL   | internalError| _end_        | true  | impossibile passare a stato _end

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
      | @clientId-delivery | PAPER   | booked       | anyStatus          | true  |
      | @clientId-delivery | PAPER   | sent         | anyStatus          | true  |
      | @clientId-delivery | PAPER   | retry        | error              | true  |
      | @clientId-delivery | PAPER   | duplicatedRequest | anyStatus     | true  |
    #TEST ERROR CARTACEO
      | @clientId-delivery | PAPER   | _start_      | sent               | false |
    #TEST _ANY_TO_ANY CARTACEO DA RIVEDERE????
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
      | @clientId-delivery | SMS      | booked      | sent               | true  |
      | @clientId-delivery | SMS      | retry       | sent               | true  |
    #ERROR SMS
      | @clientId-delivery | SMS      | _start_     | sent               | false |

  @PnStateMachineValidateExternalStatus @getClient @getProcess @getStatus @getExternalStatus @getLogicStatus
  Scenario Outline: Ottieni ExternalStatus e LogicStatus tramite chiamata GET all'endpoint
    Given a "<clientId>"
    When submit a "<status>" of a "<process>"
    Then i get "<externalStatus>" and "<logicStatus>"
    Examples:
      | clientId           | process | status            | externalStatus| logicStatus |
   #SMS
      | @clientId-delivery | SMS      | booked 	       	 | PROGRESS      | null        |
      | @clientId-delivery | SMS      | sent   	       	 | OK            | S003        |
      | @clientId-delivery | SMS      | retry  	       	 | PROGRESS      | null        |
      | @clientId-delivery | SMS      | error  	       	 | ERROR         | S008        |
      | @clientId-delivery | SMS      | internalError  	 | ERROR      	 | S010        |
    #EMAIL
      | @clientId-delivery | EMAIL    | booked 		   	 | PROGRESS      | null        |
      | @clientId-delivery | EMAIL    | sent   		   	 | OK            | M003        |
      | @clientId-delivery | EMAIL    | retry  		   	 | PROGRESS      | null        |
      | @clientId-delivery | EMAIL    | error  		   	 | ERROR         | M008        |
      | @clientId-delivery | EMAIL    | internalError  	 | ERROR         | M010        |
      | @clientId-delivery | EMAIL    | compError      	 | ERROR         | M011        |

    #PEC
      | @clientId-delivery | PEC      | booked   	   	 | PROGRESS      | null        |
      | @clientId-delivery | PEC      | sent     	   	 | PROGRESS      | C000        |
      | @clientId-delivery | PEC      | retry    	   	 | PROGRESS      | null        |
      | @clientId-delivery | PEC      | error    	   	 | ERROR         | C008        |
      | @clientId-delivery | PEC      | accepted 	   	 | PROGRESS      | C001        |
      | @clientId-delivery | PEC      | delivered		 | OK  	         | C003        |
      | @clientId-delivery | PEC      | addressError   	 | ERROR         | C011        |
      | @clientId-delivery | PEC      | infected       	 | ERROR         | C006        |
      | @clientId-delivery | PEC      | notAccepted    	 | ERROR         | C002        |
      | @clientId-delivery | PEC      | notDelivered   	 | ERROR         | C004        |
      | @clientId-delivery | PEC      | deliveryWarn   	 | PROGRESS      | C007        |
      | @clientId-delivery | PEC      | nonPEC         	 | ERROR         | C009        |
      | @clientId-delivery | PEC      | internalError  	 | ERROR         | C010        |
    #CARTACEO - in cartaceo non abbiamo uno status a OK
      | @clientId-delivery | PAPER    | booked         	 | PROGRESS      | null        |
      | @clientId-delivery | PAPER    | sent           	 | PROGRESS      | P000        |
      | @clientId-delivery | PAPER    | retry          	 | PROGRESS      | null        |
      #ALCUNI ERRORI NON HANNO IL LOGICSTATUS COME DA DOCUMENTAZIONE
      | @clientId-delivery | PAPER   | inprogress          | PROGRESS       | P001        |
      | @clientId-delivery | PAPER   | syntaxError         | ERROR          | P011        |
      | @clientId-delivery | PAPER   | semanticError       | ERROR          | P012        |
      | @clientId-delivery | PAPER   | transformationError | ERROR          | P013        |
      #errori senza LOGICSTATUS
      | @clientId-delivery | PAPER    | error            | ERROR         | null        |
      | @clientId-delivery | PAPER    | internalError    | ERROR         | null        |
      | @clientId-delivery | PAPER    | duplicatedRequest| PROGRESS      | null        |
      | @clientId-delivery | PAPER    | authenticationError| ERROR       | null        |
    #SERCQ
      | @clientId-delivery | SERCQ    | booked           | PROGRESS      | null        |
      | @clientId-delivery | SERCQ    | sent             | OK            | Q003        |
      | @clientId-delivery | SERCQ    | internalError    | ERROR         | Q010        |
      | @clientId-delivery | SERCQ    | addressError     | ERROR         | Q011        |

