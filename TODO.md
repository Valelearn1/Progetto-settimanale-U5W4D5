# Stato del progetto — Bacheca

Social network didattico. Backend Spring Boot + frontend React.
Aggiornato: 11 settembre 2026.

---

## I 4 requisiti del progetto

| # | Requisito | Backend | Frontend |
|---|-----------|---------|----------|
| 1 | Post con foto da fotocamera **o** upload multiplo | ✅ | ✅ |
| 2 | Validazione formato file su **entrambi** i lati | ✅ | ✅ |
| 3 | Posizione sul post (indirizzo o punto sulla mappa) | ✅ | ✅ |
| 4 | Documenti con testo estratto da OCR (profilo **e** allegati ai post) | ✅ | ✅ |

Tutti e quattro i requisiti sono coperti end-to-end.

---

## Backend — fasi dell'ordine di sviluppo

- [x] **1. Configurazione properties** — datasource, limiti upload, storage, OCR, geocoding
- [x] **2. CORS su 5173** — GET, POST, PUT, PATCH, DELETE, OPTIONS
- [x] **3. Verifica WebSocket** — testato con round-trip STOMP reale, controller di test poi rimosso
- [x] **4. Creazione DB** — `Progetto-settimanale-U5W4D5`, 4 tabelle generate da Hibernate
- [x] **5. Post con foto** — 7 endpoint, storage su disco, doppia validazione
- [x] **6. Mappa / geocoding** — mappa e geocoding Google (API v4, chiave gratuita)
- [x] **7. OCR** — Tesseract + PDFBox, sincrono
- [ ] **8. Registrazione / Login** — da fare
- [ ] **9. Security / JWT** — da fare

### Entità (4 tabelle)

- [x] `User` → `users` — id UUID, nomeCompleto, username, email, password, createdAt
- [x] `Post` → `posts` — text, location embedded, captureMode, timestamps, FK utente
- [x] `Location` — embedded in `posts`: latitude/longitude `BigDecimal`, address
- [x] `Photo` → `photos` — filePath, contentType, sizeBytes, position (nessuna coordinata)
- [x] `Document` → `documents` — filePath, contentType, sizeBytes, extractedText, FK post (nullable)

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

- [x] Design system a variabili CSS — tema chiaro e scuro, interruttore
- [x] Layout con navigazione (Feed / Pubblica / Profilo)
- [x] Client API con gestione errori dal backend
- [x] Validazione file con magic bytes
- [x] Hook fotocamera con spegnimento stream e errori gestiti
- [x] Composer in stile social in cima alla bacheca, con barra strumenti sempre visibile (Foto / Documento / Luogo)
- [x] Selettore posizione con **Google Maps**: ricerca, click sulla mappa, GPS
- [x] Drag & drop per foto e documenti
- [x] Feed con card, galleria foto adattiva, indirizzo
- [x] Profilo con upload documenti e testo OCR (da file o fotocamera)
- [x] Correzione a mano del testo estratto dall'OCR
- [x] Documenti allegabili a un post, da file o fotocamera, visibili nel feed

### Da fare / migliorabile

- [ ] Pagine di login e registrazione (dipendono dalle fasi 8-9)
- [ ] Paginazione del feed: ora carica i primi 20 e basta, manca "carica altri"
- [ ] Interfaccia per `PUT /api/posts/{id}` (modifica post): endpoint pronto, schermata no
- [ ] Interfaccia per aggiungere/rimuovere foto da un post esistente: endpoint pronti, schermata no
- [ ] Conferma prima di eliminare un documento (sul post c'è già)

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
- Chiave Google Maps in `FEJSX/.env.local` (copiare da `.env.example`)

---

## Note aperte

- **Google Maps**: mappa e geocoding funzionano entrambi con la chiave
  gratuita, senza fatturazione. Il geocoding usa la **API v4**
  (`geocode.googleapis.com/v4/...`): quella classica invece richiede il
  billing attivo. Nominatim resta disponibile come alternativa mettendo
  `app.geocoding.provider=nominatim` nelle properties.
- La chiave Maps sta in `FEJSX/.env.local`, che non è versionato. Chi clona il
  progetto deve copiare `.env.example` in `.env.local` e inserire la propria.
- **Credenziali del database** sono scritte in chiaro in
  `application.properties`. Prima della consegna converrebbe spostarle in
  variabili d'ambiente.
- L'utente è fisso (`demo`, creato all'avvio) finché non ci sono le fasi 8-9.
