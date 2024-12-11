# Contribuire a uozap

Questo file contiene le linee guida per organizzare il lavoro, strutturare il repository, e mantenere uno stile uniforme durante lo sviluppo del codice.

---

## 1. **Struttura del Repository**
Per mantenere il repository ordinato e comprensibile, segui queste linee guida per posizionare i file:

- **Diagrammi UML:**
  - Posizionare i diagrammi UML nelle rispettive cartelle della documentazione:
    - Inglese: `docs/en/diagrams/`
    - Italiano: `docs/it/diagrams/`
  - Assicurarsi che ogni diagramma abbia una versione sia in inglese che in italiano. Esempio:
    ```
    docs/en/diagrams/class_diagram.png
    docs/it/diagrams/class_diagram.png
    ```

- **Codice sorgente:**
  - Il codice sorgente del progetto va nella directory `src/`:
    - `src/client/` per il codice del client.
    - `src/server/` per il codice del server.
    - eccetera.

- **Test:**
  - Posizionare i test nella directory `tests/`:
    - `tests/unit/` per i test unitari.
    - `tests/integration/` per i test di integrazione.

---

## 2. **Branch Workflow**
Utilizziamo un sistema di branching per facilitare lo sviluppo collaborativo. Segui queste regole:

- **Branch principali:**
  - `main`: Contiene solo il codice stabile e pronto per la consegna.
  - `develop`: Contiene il codice in sviluppo, con le funzionalità integrate e testate.

- **Branch funzionali:**
  - Ogni nuova funzionalità o modifica deve essere sviluppata in un branch dedicato.
  - Nomenclatura:
    - `feature/<nome-funzionalità>`: Per lo sviluppo di nuove funzionalità.
  - Esempi:
    - `feature/server-multithreading`
    - `feature/client-gui`

- **Pull Request:**
  - Quando una funzionalità è completa, crea una pull request per integrare il tuo branch in `develop`.
  - Le pull request devono essere riviste da almeno un altro membro del team.

---

## 3. **Stile e Commenti del Codice**
Per mantenere uno stile uniforme, visto che servirà creare javadoc, segui queste linee guida:
- **Nomenclatura:**
  - Utilizzare nomi di variabili, metodi e classi in **inglese**.
  - Usare il **camelCase** per nomi di metodi e variabili (es. `sendMessage`, `userCount`).
  - Usare il **PascalCase** per nomi di classi (es. `ChatServer`, `UserManager`).

- **Commenti:**
  - Scrivere i commenti in **inglese**.
  - Includere una descrizione chiara per ogni metodo e classe utilizzando il formato **Javadoc**.

Esempio di Javadoc:
```java
/**
 * handles the connections and message routing for the chat server.
 */
public class ChatServer {

    /**
     * starts the server and listens for incoming client connections.
     *
     * @param port the port number on which the server will listen
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public void startServer(int port) throws IOException {
        // implementation here
    }
}
```
> **_NOTA:_** in questo esempio i commenti sono ESAGERATI per rendere l'idea

---

## 4. **Contributi Generali**

- Scrivi commit brevi e significativi in inglese. Utilizza la seguente convenzione:
  - `feat: <descrizione>` per nuove funzionalità.
  - `fix: <descrizione>` per correzioni di bug.
  - `docs: <descrizione>` per modifiche alla documentazione.
  - `test: <descrizione>` per aggiunte o modifiche ai test.

Esempio:
```text
feat: implement multi-threading in server
fix: resolve null pointer exception in message handler
```