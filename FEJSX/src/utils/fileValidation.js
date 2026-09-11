/*
  Validazione dei file lato frontend.

  ATTENZIONE: questi controlli servono a dare un riscontro immediato a
  chi usa l'app, NON a garantire la sicurezza. Chiunque puo' aprire
  Postman e chiamare l'API saltando del tutto questo file. La
  validazione che conta e' quella del backend (ImageFileValidator e
  DocumentFileValidator), che rifa' tutti i controlli leggendo il
  contenuto reale del file.
*/

export const MAX_IMAGE_BYTES = 5 * 1024 * 1024 // 5 MB
export const MAX_DOCUMENT_BYTES = 10 * 1024 * 1024 // 10 MB

export const IMAGE_ACCEPT = 'image/jpeg,image/png,image/webp'
export const DOCUMENT_ACCEPT = 'application/pdf,image/jpeg,image/png,image/tiff'

const IMAGE_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const IMAGE_EXTENSIONS = ['jpg', 'jpeg', 'png', 'webp']

const DOCUMENT_MIME_TYPES = ['application/pdf', 'image/jpeg', 'image/png', 'image/tiff']
const DOCUMENT_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png', 'tif', 'tiff']

/*
  I "magic bytes" sono i primi byte di un file, che ne dichiarano il
  formato reale indipendentemente dal nome. Controllarli qui smaschera
  subito un file rinominato (es. un .txt chiamato foto.jpg) senza
  nemmeno inviarlo al server.
*/
const SIGNATURES = [
  { mime: 'image/jpeg', bytes: [0xff, 0xd8, 0xff] },
  { mime: 'image/png', bytes: [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a] },
  { mime: 'application/pdf', bytes: [0x25, 0x50, 0x44, 0x46] }, // "%PDF"
  { mime: 'image/tiff', bytes: [0x49, 0x49, 0x2a, 0x00] }, // little endian
  { mime: 'image/tiff', bytes: [0x4d, 0x4d, 0x00, 0x2a] }, // big endian
]

function matches(header, bytes, offset = 0) {
  return bytes.every((byte, i) => header[offset + i] === byte)
}

async function detectMimeType(file) {
  const header = new Uint8Array(await file.slice(0, 16).arrayBuffer())

  // WebP e' un caso a parte: "RIFF" nei primi 4 byte e "WEBP" a
  // partire dall'ottavo, con la dimensione del file in mezzo.
  const isRiff = matches(header, [0x52, 0x49, 0x46, 0x46])
  const isWebp = matches(header, [0x57, 0x45, 0x42, 0x50], 8)
  if (isRiff && isWebp) return 'image/webp'

  return SIGNATURES.find((signature) => matches(header, signature.bytes))?.mime ?? null
}

export function extensionOf(fileName) {
  if (!fileName.includes('.')) return ''
  return fileName.slice(fileName.lastIndexOf('.') + 1).toLowerCase()
}

export function formatBytes(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

async function validate(file, { mimeTypes, extensions, maxBytes, label }) {
  if (file.size === 0) {
    return { ok: false, error: 'il file è vuoto' }
  }
  if (file.size > maxBytes) {
    return {
      ok: false,
      error: `pesa ${formatBytes(file.size)}, il massimo è ${formatBytes(maxBytes)}`,
    }
  }

  const extension = extensionOf(file.name)
  if (!extensions.includes(extension)) {
    return { ok: false, error: `estensione .${extension} non ammessa` }
  }
  if (file.type && !mimeTypes.includes(file.type)) {
    return { ok: false, error: `tipo ${file.type} non ammesso` }
  }

  const detected = await detectMimeType(file)
  if (!detected) {
    return { ok: false, error: `non sembra un ${label} valido` }
  }
  if (!mimeTypes.includes(detected)) {
    return { ok: false, error: `è in realtà un file ${detected}` }
  }
  // Il tipo dichiarato dal browser non coincide col contenuto reale:
  // e' esattamente il caso del file rinominato a mano.
  if (file.type && file.type !== detected) {
    return { ok: false, error: `dice di essere ${file.type} ma è ${detected}` }
  }

  return { ok: true, detectedType: detected }
}

export function validateImage(file) {
  return validate(file, {
    mimeTypes: IMAGE_MIME_TYPES,
    extensions: IMAGE_EXTENSIONS,
    maxBytes: MAX_IMAGE_BYTES,
    label: 'immagine',
  })
}

export function validateDocument(file) {
  return validate(file, {
    mimeTypes: DOCUMENT_MIME_TYPES,
    extensions: DOCUMENT_EXTENSIONS,
    maxBytes: MAX_DOCUMENT_BYTES,
    label: 'documento',
  })
}

// Valida piu' file insieme e separa quelli buoni da quelli scartati,
// cosi' l'interfaccia puo' mostrare il motivo per ciascuno.
export async function validateImages(files) {
  const results = await Promise.all(
    files.map(async (file) => ({ file, ...(await validateImage(file)) })),
  )
  return {
    accepted: results.filter((r) => r.ok).map((r) => r.file),
    rejected: results
      .filter((r) => !r.ok)
      .map((r) => ({ name: r.file.name, error: r.error })),
  }
}
