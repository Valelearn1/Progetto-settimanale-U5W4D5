# Stato del progetto — Golden Hour

Social network didattico. Backend Spring Boot + frontend React.
Aggiornato: 11 settembre 2026.

---

## I 4 requisiti del progetto

| # | Requisito | Backend | Frontend |
|---|-----------|---------|----------|
| 1 | Post con foto da fotocamera **o** upload multiplo | ✅ | ✅ |
| 2 | Validazione formato file su **entrambi** i lati | ✅ | ✅ |
| 3 | Posizione sul post (indirizzo o punto sulla mappa) | ✅ | ✅ |
| 4 | Documenti sul profilo con testo estratto da OCR | ✅ | ✅ |

Tutti e quattro i requisiti sono coperti end-to-end.

---

## Backend — fasi dell'ordine di sviluppo

- [x] **1. Configurazione properties** — datasource, limiti upload, storage, OCR, geocoding
- [x] **2. CORS su 5173** — GET, POST, PUT, PATCH, DELETE, OPTIONS
- [x] **3. Verifica WebSocket** — testato con round-trip STOMP reale, controller di test poi rimosso
- [x] **4. Creazione DB** — `Progetto-settimanale-U5W4D5`, 4 tabelle generate da Hibernate
- [x] **5. Post con foto** — 7 endpoint, storage su disco, doppia validazione
- [x] **6. Mappa / geocoding** — Nominatim attivo, Google pronto ma disattivato
- [x] **7. OCR** — Tesseract + PDFBox, sincrono
- [ ] **8. Registrazione / Login** — da fare
- [ ] **9. Security / JWT** — da fare

### Entità (4 tabelle)

- [x] `User` → `users` — id UUID, nomeCompleto, username, email, password, createdAt
- [x] `Post` → `posts` — text, location embedded, captureMode, timestamps, FK utente
- [x] `Location` — embedded in `posts`: latitude/longitude `BigDecimal`, address
- [x] `Photo` → `photos` — filePath, contentType, sizeBytes, position (nessuna coordinata)
- [x] `Document` → `documents` — filePath, contentType, sizeBytes, extractedText

### Endpoint

**Post** — tutti implementati e testati con curl
- [x] `POST /api/posts` — crea post (multipart: JSON + foto)
- [x] `GET /api/posts` — lista paginata, più recenti prima
- [x] `GET /api/posts/{id}` — dettaglio
- [x] `PUT /api/posts/{id}` — modifica testo e posizione
- [x] `DELETE /api/posts/{id}` — cancella record **e** file dal disco
- [x] `POST /api/posts/{id}/photos` — aggiunge una foto
- [x] `DELETE /api/posts/{id}/photos/{photoId}` — rimuove una foto

**Geo** — testati dal vivo contro Nominatim
- [x] `GET /api/geo/search?q=` — ricerca indirizzo
- [x] `GET /api/geo/reverse?lat=&lng=` — coordinate → indirizzo

**Documenti** — testati dal vivo con OCR reale
- [x] `POST /api/documents` — carica + estrae testo
- [x] `GET /api/documents` — i miei documenti
- [x] `GET /api/documents/{id}` — dettaglio con testo
- [x] `DELETE /api/documents/{id}` — cancella record e file

**Auth** — da fare (fasi 8-9)
- [ ] `POST /api/auth/register`
- [ ] `POST /api/auth/login`

---

## Frontend

- [x] Design system "tramonto in spiaggia" — tema chiaro e scuro, interruttore
- [x] Layout con navigazione (Feed / Pubblica / Profilo)
- [x] Client API con gestione errori dal backend
- [x] Validazione file con magic bytes
- [x] Hook fotocamera con spegnimento stream e errori gestiti
- [x] Form post: tab Scatta/Carica, anteprime, rimozione, contatore caratteri
- [x] Selettore posizione: ricerca, click sulla mappa, GPS
- [x] Feed con card, galleria foto adattiva, indirizzo
- [x] Profilo con upload documenti e testo OCR

### Da fare / migliorabile

- [ ] **Verificare nel browser** i flussi fotocamera, upload e mappa (finora provata solo la pagina feed)
- [ ] Pagine di login e registrazione (dipendono dalle fasi 8-9)
- [ ] Paginazione del feed: ora carica i primi 20 e basta, manca "carica altri"
- [ ] Interfaccia per `PUT /api/posts/{id}` (modifica post): endpoint pronto, schermata no
- [ ] Interfaccia per aggiungere/rimuovere foto da un post esistente: endpoint pronti, schermata no
- [ ] Conferma prima di eliminare un post o un documento (ora cancella subito)

---

## Test

- [x] 7 test JUnit verdi (parsing geocoding Google + Nominatim, avvio contesto)
- [x] Post con foto: 11 scenari provati con curl, inclusi i casi di errore
- [x] File di testo rinominato `.jpg` → rifiutato con 415
- [x] Fotocamera con 2 foto → rifiutato con 400
- [x] Coordinate fuori range → 400
- [x] OCR su immagine e su PDF → testo estratto corretto
- [x] Rate limit Nominatim → 3 chiamate in 3 secondi
- [ ] Test automatici dei controller (per ora verificati a mano con curl)

---

## Prerequisiti per far girare il progetto

- PostgreSQL in locale, database `Progetto-settimanale-U5W4D5`
- Tesseract installato (`brew install tesseract tesseract-lang`) — già presente
- Backend su `localhost:8080`, frontend su `localhost:5173`
- Nessuna chiave API necessaria: le mappe usano OpenStreetMap

---

## Note aperte

- **Google Maps** resta disponibile ma spento: richiede un account di
  fatturazione con carta anche per la fascia gratuita. Per riattivarlo basta
  mettere `app.geocoding.provider=google` nelle properties.
- **Credenziali del database** sono scritte in chiaro in
  `application.properties`. Prima della consegna converrebbe spostarle in
  variabili d'ambiente.
- L'utente è fisso (`demo`, creato all'avvio) finché non ci sono le fasi 8-9.
