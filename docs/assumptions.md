# Architecture and Scoping Assumptions

## 1. Database: MySQL
MySQL was chosen as requested. Soft-deletes are heavily utilized on the `tasks` table (`deleted_at` column) to ensure that the immutable ledger (which relies on Foreign Keys to the `tasks` table) does not orphan its history records. 

## 2. Blockchain-Style Ledger
**Assumption**: A real distributed blockchain network (like Ethereum or Hyperledger) is outside the scope of a 1-2 day assignment.
**Implementation**: We implemented a centralized, lightweight cryptographic hash chain inside MySQL (`task_ledger` table). It simulates blockchain immutability principles by snapshotting the task state, generating a SHA-256 hash using the previous hash in the chain, and providing an endpoint to cryptographically verify the integrity of the chain. If a malicious actor alters a record directly in the MySQL database, the Verification API will immediately flag the entire chain as "Tampered".

## 3. Gemini API Integration
**Assumption**: Prompts that ask for JSON in the text body are brittle.
**Implementation**: We utilized Gemini's native `generationConfig.responseMimeType = "application/json"` and passed a strict JSON Schema (`responseSchema`). This forces the model to return a syntactically correct JSON object, eliminating the need for complex regex parsing.
**Fallback**: If the API key is missing or the call times out, the backend gracefully falls back to empty/default strings and flags `aiGenerated: false`, ensuring the user is never blocked from creating a task.

## 4. Frontend Security
**Assumption**: XSS vs CSRF tradeoffs.
**Implementation**: For the scope of this assignment, the JWT is stored in Zustand (in-memory state) and persisted via `localStorage`. While susceptible to XSS, it avoids the complexity of setting up HTTP-only cookies and CSRF tokens across different deployment domains (Vercel vs Railway) for a take-home project. In a production environment, `HttpOnly` cookies would be strongly recommended.
