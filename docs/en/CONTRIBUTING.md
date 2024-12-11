# Contributing to uozap
This file contains the guidelines for organizing work, structuring the repository, and maintaining a consistent style during code development.

---

## 1. **Repository Structure**
To keep the repository organized and understandable, follow these guidelines for file placement:

- **UML Diagrams:**
  - Place UML diagrams in the respective documentation folders:
    - English: `docs/en/diagrams/`
    - Italian: `docs/it/diagrams/`
  - Ensure that each diagram has a version in both English and Italian. Example:
    ```
    docs/en/diagrams/class_diagram.png
    docs/it/diagrams/class_diagram.png
    ```

- **Source Code:**
  - Project source code goes into the `src/` directory:
    - `src/client/` for client code.
    - `src/server/` for server code.
    - etc.

- **Tests:**
  - Place tests in the `tests/` directory:
    - `tests/unit/` for unit tests.
    - `tests/integration/` for integration tests.

---

## 2. **Branch Workflow**
We use a branching system to facilitate collaborative development. Follow these rules:

- **Main Branches:**
  - `main`: Contains only stable, delivery-ready code.
  - `develop`: Contains development code with integrated and tested features.

- **Feature Branches:**
  - Each new feature or modification must be developed in a dedicated branch.
  - Naming convention:
    - `feature/<feature-name>`: For developing new features.
  - Examples:
    - `feature/server-multithreading`
    - `feature/client-gui`

- **Pull Requests:**
  - When a feature is complete, create a pull request to merge your branch into `develop`.
  - Pull requests must be reviewed by at least one other team member.

---

## 3. **Code Style and Comments**
To maintain a consistent style, as Javadoc will be needed, follow these guidelines:
- **Naming Conventions:**
  - Use variable, method, and class names in **English**.
  - Use **camelCase** for method and variable names (e.g., `sendMessage`, `userCount`).
  - Use **PascalCase** for class names (e.g., `ChatServer`, `UserManager`).

- **Comments:**
  - Write comments in **English**.
  - Include a clear description for each method and class using the **Javadoc** format.

Javadoc Example:
```java
/**
 * Handles the connections and message routing for the chat server.
 */
public class ChatServer {

    /**
     * Starts the server and listens for incoming client connections.
     *
     * @param port the port number on which the server will listen
     * @throws IOException if an I/O error occurs when opening the socket
     */
    public void startServer(int port) throws IOException {
        // implementation here
    }
}
```

> **_NOTE:_** in this example the comments are exagerated to give the idea

---

## 4. **General Contributions**

- Write short and clear commits. Use the following format:
  - `feat: <descrizione>` for new features.
  - `fix: <descrizione>` for bug fixes.
  - `docs: <descrizione>` for documentation updates.
  - `test: <descrizione>` for implementing tests.

Example:
```text
feat: implement multi-threading in server
fix: resolve null pointer exception in message handler
```