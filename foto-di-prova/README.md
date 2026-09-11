<div align="center">

# 🌅 Foto di prova

**Immagini pronte all'uso per testare Golden Hour**

![Formati](https://img.shields.io/badge/formati-JPEG%20%7C%20PNG-ff6b35?style=for-the-badge)
![Limite](https://img.shields.io/badge/limite-5%20MB%20per%20foto-ffb627?style=for-the-badge)
![Licenza](https://img.shields.io/badge/copyright-nessun%20vincolo-1b9aaa?style=for-the-badge)

*Generate via codice, quindi liberamente utilizzabili* ✨

</div>

---

## 🖼️ La galleria

<table>
  <tr>
    <td width="33%" align="center">
      <img src="tramonto-sul-mare.jpg" width="100%" alt="Tramonto sul mare" /><br />
      <b>tramonto-sul-mare.jpg</b><br />
      <sub>JPEG · 1600×1067 · 82 KB</sub>
    </td>
    <td width="33%" align="center">
      <img src="spiaggia-con-palme.jpg" width="100%" alt="Spiaggia con palme" /><br />
      <b>spiaggia-con-palme.jpg</b><br />
      <sub>JPEG · 1600×1067 · 93 KB</sub>
    </td>
    <td width="33%" align="center">
      <img src="riflessi-sull-acqua.jpg" width="100%" alt="Riflessi sull'acqua" /><br />
      <b>riflessi-sull-acqua.jpg</b><br />
      <sub>JPEG · 1920×1280 · 115 KB</sub>
    </td>
  </tr>
  <tr>
    <td width="33%" align="center">
      <img src="ultima-luce.png" width="100%" alt="Ultima luce" /><br />
      <b>ultima-luce.png</b><br />
      <sub>PNG · 1280×854 · 88 KB</sub>
    </td>
    <td width="33%" align="center">
      <img src="battigia.png" width="100%" alt="Battigia" /><br />
      <b>battigia.png</b><br />
      <sub>PNG · 1100×733 · 74 KB</sub>
    </td>
    <td width="33%" align="center">
      <br /><br />
      🏖️<br /><br />
      <sub>Tutte sotto i 5 MB<br />Caricabili singolarmente<br />o fino a 10 insieme</sub>
      <br /><br />
    </td>
  </tr>
</table>

> 💡 **Come usarle:** vai su **Pubblica**, scegli la scheda **Carica** e
> trascinale nell'area tratteggiata, oppure cliccala per aprire il
> selettore file.

---

## 🧪 I due file per testare la validazione

Questi non sono foto vere: servono a dimostrare che i controlli sui
formati funzionano davvero.

### 🎭 `finta-foto.jpg` — il travestimento

![Peso](https://img.shields.io/badge/peso-79%20byte-lightgrey?style=flat-square)
![Atteso](https://img.shields.io/badge/risposta%20attesa-415-d7263d?style=flat-square)

È un **file di testo rinominato** in `.jpg`. Il nome dice "immagine",
il contenuto no.

| Dove | Cosa succede |
|:---|:---|
| 🖥️ **Frontend** | Bloccato **prima dell'invio**. Legge i primi byte del file (i *magic bytes*) e vede che non corrispondono a nessun formato immagine |
| ⚙️ **Backend** | Rifiutato con **415** anche saltando il frontend. Non si fida né dell'estensione né del tipo dichiarato: legge il contenuto reale con Apache Tika e prova a decodificarlo |

**Provalo direttamente sul backend**, bypassando il frontend:

```bash
curl -X POST http://localhost:8080/api/posts \
  -F 'post={"text":"prova","captureMode":"UPLOAD"};type=application/json' \
  -F 'photos=@foto-di-prova/finta-foto.jpg'
```

Risposta:

```json
{
  "status": 415,
  "message": "Uno o piu' file non sono validi",
  "details": ["finta-foto.jpg: Formato file non supportato: text/plain"]
}
```

### 🏋️ `troppo-grande.jpg` — oltre il limite

![Peso](https://img.shields.io/badge/peso-5.3%20MB-orange?style=flat-square)
![Limite](https://img.shields.io/badge/limite-5%20MB-ffb627?style=flat-square)
![Atteso](https://img.shields.io/badge/risposta%20attesa-413-d7263d?style=flat-square)

Un'immagine **vera**, ma troppo pesante. Il frontend la blocca dicendo
quanto pesa e qual è il massimo consentito. Il backend ha un limite suo
di 10 MB per file (in `application.properties`) e risponde **413** se
viene superato.

Contiene rumore casuale apposta: non si comprime, così bastano
dimensioni contenute per superare i 5 MB senza appesantire inutilmente
il repository.

---

## 🔐 Perché due controlli e non uno

<div align="center">

| | 🖥️ Frontend | ⚙️ Backend |
|:---|:---|:---|
| **A cosa serve** | All'utente | Alla sicurezza |
| **Cosa dà** | Risposta immediata, niente attesa inutile | L'unica barriera che conta |
| **Si può aggirare?** | ✅ Sì, con Postman | ❌ No |

</div>

> ⚠️ Chiunque può saltare il frontend e chiamare l'API direttamente.
> Per questo il backend **rifà tutti i controlli da zero**, leggendo il
> contenuto reale del file invece di fidarsi di quello che gli viene
> dichiarato.

---

<div align="center">
<sub>🌅 Golden Hour · progetto didattico · React + Spring Boot</sub>
</div>
