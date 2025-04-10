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

## Modalità di esecuzione

Per eseguire i test sull'ambiente reale, è necessario avere un tunnel SSM attivo verso l'infrastruttura di destinazione.

Assicurati di avviare correttamente la sessione SSM (ad esempio tramite aws ssm start-session) prima di eseguire i test, così che il traffico venga instradato correttamente verso i servizi interni.

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
