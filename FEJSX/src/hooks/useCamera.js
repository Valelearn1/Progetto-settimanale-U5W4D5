import { useCallback, useEffect, useRef, useState } from 'react'

// Messaggi leggibili al posto dei nomi tecnici degli errori del browser.
const ERROR_MESSAGES = {
  NotAllowedError:
    'Permesso negato. Autorizza la fotocamera dalle impostazioni del browser e riprova.',
  NotFoundError: 'Nessuna fotocamera trovata su questo dispositivo.',
  NotReadableError:
    'La fotocamera è già in uso da un altro programma. Chiudilo e riprova.',
  OverconstrainedError: 'Nessuna fotocamera compatibile con le impostazioni richieste.',
  SecurityError: 'La fotocamera richiede una connessione sicura (https o localhost).',
}

export function useCamera() {
  const videoRef = useRef(null)
  const streamRef = useRef(null)
  const [active, setActive] = useState(false)
  const [starting, setStarting] = useState(false)
  const [error, setError] = useState(null)

  // Spegne davvero la webcam. Senza questo la spia del computer resta
  // accesa anche dopo aver chiuso la schermata.
  const stop = useCallback(() => {
    streamRef.current?.getTracks().forEach((track) => track.stop())
    streamRef.current = null
    if (videoRef.current) videoRef.current.srcObject = null
    setActive(false)
  }, [])

  const start = useCallback(async () => {
    setError(null)

    if (!navigator.mediaDevices?.getUserMedia) {
      setError(
        'Questo browser non espone la fotocamera. Serve una connessione sicura (https o localhost).',
      )
      return
    }

    setStarting(true)
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        // "environment" = fotocamera posteriore sui telefoni; sui
        // computer viene semplicemente ignorato.
        video: { facingMode: 'environment' },
        audio: false,
      })
      streamRef.current = stream
      if (videoRef.current) {
        videoRef.current.srcObject = stream
        await videoRef.current.play()
      }
      setActive(true)
    } catch (err) {
      setError(ERROR_MESSAGES[err.name] ?? `Impossibile aprire la fotocamera (${err.name}).`)
      stop()
    } finally {
      setStarting(false)
    }
  }, [stop])

  /*
    Cattura il fotogramma corrente e lo trasforma in un File JPEG,
    identico a uno scelto dal disco: da qui in poi segue esattamente
    lo stesso percorso di validazione e invio di una foto caricata.
  */
  const capture = useCallback(async () => {
    const video = videoRef.current
    if (!video || !video.videoWidth) return null

    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    canvas.getContext('2d').drawImage(video, 0, 0)

    const blob = await new Promise((resolve) =>
      canvas.toBlob(resolve, 'image/jpeg', 0.9),
    )
    if (!blob) return null

    const fileName = `scatto-${Date.now()}.jpg`
    return new File([blob], fileName, { type: 'image/jpeg' })
  }, [])

  // Se il componente sparisce mentre la camera e' accesa, la spegne.
  useEffect(() => stop, [stop])

  return { videoRef, active, starting, error, start, stop, capture }
}
