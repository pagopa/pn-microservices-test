# Guida all'utilizzo della suite di test `pn-microservices-test`

Questa suite di test è progettata per verificare il corretto funzionamento dei microservizi BING (pn-ec, pn-ss, pn-statemachinemanager e pn-pdfraster) tramite test comportamentali (BDD) basati su Cucumber.

## Requisiti

- Java 17 o superiore
- [Session Manager Plugin](https://docs.aws.amazon.com/systems-manager/latest/userguide/session-manager-working-with-install-plugin.html) di AWS (mandatorio per i test su ambiente reale)
- [Plugin Gherkin per IntelliJ IDEA](https://plugins.jetbrains.com/plugin/9164-gherkin) (opzionale, per eseguire scenari singoli)

## Clonazione del progetto

```bash
git clone https://github.com/pagopa/pn-microservices-test.git
cd pn-microservices-test
```

## Configurazione

### File di properties

La configurazione dell'ambiente viene gestita tramite file `.properties`:

- Il file principale è `application.properties`
- Può fare riferimento a profili specifici impostando la property:

```properties
spring.profiles.active=dev
```

In questo caso, verrà caricato automaticamente anche il file `application-dev.properties`.

### Uso delle variabili di sistema

All'interno degli scenari Gherkin, i parametri prefissati con `@` fanno riferimento a una property di sistema.  
Esempio:

```gherkin
Given invio una richiesta POST con body @requestBody
```

In questo caso, il valore effettivo di `requestBody` sarà cercato tra le system properties o quelle definite nei file `.properties`.

### Chiave API del consolidatore nell'ambiente TEST

Nel profilo `test` la chiave API del client consolidatore `pn-cons-000` (`apiKey-cons`) non è scritta
nel file di properties: è un dato sensibile e va fornita dall'esterno tramite la variabile d'ambiente
`APIKEY_CONS_TEST`.

```properties
apiKey-cons=${APIKEY_CONS_TEST}
```

- La variabile va impostata **solo** per l'esecuzione con il profilo `test`. Con i profili `dev` e
  `uat` la chiave è definita nel rispettivo file di properties e la variabile non viene letta.
- Con il profilo `test`, se la variabile non è impostata o è vuota, il caricamento della configurazione
  fallisce e gli scenari terminano con l'errore
  `La property apiKey-cons richiede la variabile d'ambiente APIKEY_CONS_TEST, che non è impostata`.

```bash
export APIKEY_CONS_TEST='<chiave API di pn-cons-000 in TEST>'
./mvnw test -Dspring.profiles.active=test
```

In generale, ogni property valorizzata nella forma `${NOME_VARIABILE}` viene letta dalla variabile
d'ambiente indicata, e la sua assenza blocca l'esecuzione.

## Modalità di esecuzione

Per eseguire i test sull'ambiente reale, è necessario avere un tunnel SSM attivo verso l'infrastruttura di destinazione.

Assicurati di avviare correttamente la sessione SSM (ad esempio tramite aws ssm start-session) prima di eseguire i test, così che il traffico venga instradato correttamente verso i servizi interni.

### Tunnel SSM necessari

La suite raggiunge i microservizi su `localhost`: l'URL di ogni chiamata è composto da `baseURL`
(`application.properties`) e dalla porta del servizio, definita per profilo in
`application-<profile>.properties`.

| Servizio | Property | Valore in `dev` |
|---|---|---|
| pn-ec, pn-ss, pn-statemachinemanager | `pn.ec.port`, `pn.ss.port`, `pn.sm.port` | `8080` |
| pn-pdfraster | `pn.pdfraster.port` | `8081` |
| pn-io-connector | `pn.io.port`, `pn.io.external.port` | `8080` |

`pn-pdfraster` è esposto su una **porta dedicata**, quindi non è raggiungibile attraverso il tunnel
usato per gli altri microservizi: serve una seconda sessione SSM che inoltri quella porta.

**Per eseguire `PdfRasterCucumberTest` devono essere attivi entrambi i tunnel contemporaneamente:**

- il tunnel verso l'ambiente **confinfo** (porta `8080`), usato dagli scenari `@Transformation`, che
  caricano il documento tramite l'API di upload di `pn-ss` e ne verificano l'evento di disponibilità;
- il tunnel **specifico per pn-pdfraster** (porta `8081`), usato dagli scenari sull'endpoint sincrono
  `/PDFRaster/convert`.

Con un solo tunnel attivo la suite fallisce parzialmente con `Connection refused` sugli scenari che
dipendono dall'altro.

### Test di pn-io-connector: tunnel verso core, credenziali AWS verso confinfo

`pn-io-connector` è deployato nell'account **core**, mentre `pn-ec`, `pn-ss`,
`pn-statemachinemanager` e `pn-pdfraster` sono nell'account **confinfo**. Per eseguire
`IoCucumberTest` (o un singolo feature file di `tests/io` da IntelliJ) servono quindi:

- **il tunnel SSM verso <env>_core**,`.

>Il tunnel confinfo non espone `pn-io-connector`: le chiamate a `/io-connector/*` ricevono
>`404` con body `{ "error": "404", "message": "Load balancer rule not configured" }`, cioè la
>risposta di default del load balancer, senza che la richiesta arrivi al servizio;

- **le credenziali AWS dell'account confinfo**

>`AWS_PROFILE=dev_confinfo` nella run configuration di IntelliJ), anche se il servizio testato è in core.

Il motivo del secondo punto: in Cucumber gli hook `@BeforeAll` sono globali a tutta la glue, quindi
anche quando si esegue solo la suite IO partono `EcStepDefinitions.init()` e
`SsStepDefinitions.init()`, che avviano i poller SQS sulle code di pn-ec
(`pn.ec.notifiche.esterne.queue.name`) e di pn-ss (`pn.ss.gestore.disponibilita.queue.name`).
Queste code esistono solo in confinfo: con le credenziali core l'esecuzione si interrompe prima del
primo scenario (`0 Scenarios`) con

```
java.lang.RuntimeException: Error initializing queue poller
Caused by: software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException: The specified queue does not exist.
```

Tunnel e credenziali sono indipendenti: si può avere il tunnel verso core e `AWS_PROFILE` su
confinfo nello stesso momento.

### Test di invio messaggio legale su IO (`sendLegalIoMessage.feature`)

Gli scenari di `POST /io-connector/sendmessage` non decidono da soli l'esito: pn-io-connector legge
il profilo del codice fiscale su App IO e sceglie il flusso in base allo **stato di quel CF sul
servizio SEND**. Prima di eseguirli bisogna quindi accertarsi che ogni CF configurato sia nello
stato richiesto dallo scenario.

| Scenario | CF (property) | Stato richiesto del CF | Esito atteso |
|---|---|---|---|
| Messaggio legale (`@sendLegal_courtesy`) | `io.legal.taxId.pnActive` | registrato su IO **con SEND attivo** (`PN_ACTIVE`) | `SENT_COURTESY` |
| Opt-in (`@sendLegal_optin`) | `io.legal.taxId.pnNotActive` | registrato su IO **con SEND non attivo** (`PN_NOT_ACTIVE`) | `SENT_OPTIN` |

Lo stato di un CF si verifica con lo scenario `@getIoUserStatus_ok` di `getIoUserStatus.feature`
(`POST /io-connector/user-status`) oppure, più semplicemente, dall'App IO del CF di test,
attivando o disattivando il servizio SEND.

In `dev` i due scenari usano CF distinti, già nello stato richiesto: con lo stato attuale gli esiti
sono esattamente quelli attesi dai test.

| Property | CF in `dev` | Stato SEND attuale | Esito |
|---|---|---|---|
| `io.legal.taxId.pnActive` | `EEEEEE00E00E000C` | attivo | `SENT_COURTESY` |
| `io.legal.taxId.pnNotActive` | `EEEEEE00E00E000D` | disattivato | `SENT_OPTIN` |

>Nei profili `local`, `test` e `uat` `pnActive` e `pnNotActive` puntano ancora allo stesso CF: lì i due
>scenari non possono passare nella stessa esecuzione finché non si configurano due CF distinti.

Se si ottiene un esito diverso da quello atteso, è perché qualcuno ha cambiato lo stato SEND del CF
(ad esempio attivando o disattivando il servizio dall'App IO, vedi sotto). Va riportato nello stato
della tabella:

- `SENT_OPTIN` al posto di `SENT_COURTESY`: il CF ha SEND **disattivato**;
- `SENT_COURTESY` al posto di `SENT_OPTIN`: il CF ha SEND **attivato**.

Gli altri esiti non dipendono dallo stato SEND:

- `NOT_SENT_APPIO_UNAVAILABLE`: il CF non è registrato su IO o non è in whitelist;
- `NOT_SENT_OPTIN_ALREADY_SENT`: l'opt-in a quel CF è già stato inviato e il cooldown è ancora attivo.

Il cooldown dell'opt-in è registrato nella tabella DynamoDB `pn-IOConnectorOptIn` dell'account
**core** (chiave `pk` = SHA-256 del CF), e dura `PN_IO_CONNECTOR_IO_OPTIN_MIN_DAYS` giorni, una
variabile del servizio deployato. Con il valore `0` (default, e attuale in `dev`) il cooldown è
sempre scaduto, quindi lo scenario opt-in si può rieseguire quante volte si vuole; con un valore
maggiore, dalla seconda esecuzione nel periodo di cooldown l'esito diventa
`NOT_SENT_OPTIN_ALREADY_SENT`.

Lo scenario opt-in verifica anche il record su `pn-IOConnectorOptIn`. Poiché la tabella sta in
core mentre `AWS_PROFILE` punta a confinfo (vedi sopra), il client DynamoDB della suite IO usa un
profilo AWS dedicato, letto dalla property `io.aws.profile` (`dev_core`, `test_core`, `uat_core`;
vuota per `local`). Il profilo deve esistere in `~/.aws/config` con una sessione valida
(per i profili SSO: `aws sso login --profile dev_core`); in caso contrario lo step fallisce con
`ResourceNotFoundException` o con un errore di credenziali.

#### Accesso ad App IO con un utente di test

Per controllare o cambiare lo stato di un CF su SEND e vedere i messaggi ricevuti:

1. Apri l'app IO. Nella schermata di login seleziona l'icona di info (con il punto esclamativo).
2. Tocca 5 volte la versione dell'app.
3. Scegli una qualsiasi tra "Continua con l'accesso rapido" ed "Entra con SPID o CIE ogni 30 giorni".
4. Se accedi con un utente di test, inserisci il codice fiscale come username e la password
   `***RIMOSSA***` (ad esempio CF `EEEEEE00E00E000C`, password `***RIMOSSA***`).
5. Vai al Profilo e in basso tocca 5 volte la versione: compare il menu sviluppatori.

Dopo l'invio:

- se il servizio notifiche digitali di SEND è **disattivato**, nei messaggi arriva la richiesta di
  attivazione (opt-in, scenario `@sendLegal_optin`). Se attivi il servizio, dopo poco arriva anche
  la notifica;
- se il servizio notifiche digitali di SEND è **attivato**, arriva direttamente la notifica
  (scenario `@sendLegal_courtesy`).

>Se il login non riesce o il servizio risponde `NOT_SENT_APPIO_UNAVAILABLE`, verifica che il CF
>sia presente nella whitelist degli utenti di test di IO.

La suite può essere eseguita in diversi modi:

### 1. Esecuzione completa via Maven

Esegue tutte le suite di test.

```bash
./mvnw test
```

Oppure, se Maven è installato:

```bash
mvn test
```

### 2. Esecuzione di singole suite con JUnit

Puoi eseguire singolarmente le suite principali:

- `EcCucumberTest` – suite per i test del microservizio EC
- `SsCucumberTest` – suite per i test del microservizio SS
- `PdfRasterCucumberTest` – suite per i test del microservizio PDFRaster (richiede entrambi i tunnel, vedi sopra)
- `IoCucumberTest` – suite per i test del microservizio IO Connector (tunnel verso core, credenziali AWS confinfo, vedi sopra)

Questo può essere fatto da un IDE come IntelliJ IDEA o Eclipse cliccando sul test JUnit, oppure via terminale con Maven:

```bash
mvn -Dtest=EcCucumberTest test
```

### 3. Esecuzione di scenari singoli

Utilizzando un plugin apposito, come il [plugin Gherkin per IntelliJ IDEA](https://plugins.jetbrains.com/plugin/9164-gherkin), è possibile eseguire singoli scenari direttamente dall'editor. Dopo aver installato il plugin, apri il file `.feature` desiderato e utilizza l'opzione "Run" accanto allo scenario specifico.

### 4. Esecuzione di test filtrati per tag

È possibile filtrare l'esecuzione dei test in base ai tag definiti negli scenari Gherkin impostando l'argomento `cucumber.filter.tags`. Ad esempio, per eseguire solo gli scenari con il tag `@SmokeTest`:

```bash
mvn test -Dcucumber.filter.tags="@invioPEC"
```

Per eseguire scenari con più tag, puoi utilizzare operatori logici. Ad esempio:

```bash
mvn test -Dcucumber.filter.tags="@invioPEC or @invioEMAIL"
```

Per escludere scenari:

```bash
mvn test -Dcucumber.filter.tags="not @invioSMS"
```

### Report dei test

Dopo ogni esecuzione, viene generato uno o più report HTML nella cartella:

```
target/cucumber-reports
```

Apri i file `.html` presenti in questa cartella con un browser per visualizzare i risultati dettagliati.

## Struttura del progetto

- `src/test/resources/features/`: scenari scritti in Gherkin
- `src/test/java/it/pagopa/pn/cucumber/steps`: implementazioni dei passi (`StepDefinition`)
- `application.properties`: configurazione base
- `application-<profile>.properties`: configurazioni specifiche per ambiente

## Esempi

### Impostare una variabile di sistema

Puoi passare variabili al test direttamente da linea di comando:

```bash
mvn test -DrequestBody='{"example":"value"}'
```

Oppure impostare più variabili con un file `.properties` e richiamarle nel test tramite `@chiave`.

---

## Conclusioni

Questa suite consente di automatizzare e standardizzare il testing dei microservizi utilizzando scenari leggibili e configurabili. Personalizza le proprietà in base al tuo ambiente e scegli la modalità di esecuzione più adatta alle tue esigenze.
