/**
 * Captura los valores de los controles deslizantes y solicita al servidor
 * reiniciar la simulación con los nuevos parámetros de productores,
 * consumidores y tamaño del buffer.
 */
function reiniciar() {
    const p = document.getElementById('prodSlider').value;
    const c = document.getElementById('consSlider').value;
    const s = document.getElementById('sizeSlider').value;
    fetch(`/api/config?p=${p}&c=${c}&s=${s}`, {method: 'POST'});
}

/**
 * Bucle de actualización (Polling).
 * Consulta el estado del servidor cada 200 milisegundos y actualiza el DOM
 * en tiempo real reflejando semáforos, buffer e hilos.
 */
setInterval(() => {
    fetch('/api/estado', { cache: 'no-store' })
        .then(res => res.json())
        .then(data => {
            if(!data.buffer) return;

            // 1. Semáforos (Mutex eliminado)
            document.getElementById('semVacios').innerText = data.vacios;
            document.getElementById('semLlenos').innerText = data.llenos;

            document.getElementById('cajaVacios').className = 'sem-box ' + (data.vacios === 0 ? 'rojo' : '');
            document.getElementById('cajaLlenos').className = 'sem-box ' + (data.llenos === 0 ? 'rojo' : '');

            // 2. Dibujar Bandeja de Pedidos (Buffer)
            const bufferUI = document.getElementById('bufferUI');
            bufferUI.innerHTML = '';
            data.buffer.forEach((val, index) => {
                const slot = document.createElement('div');
                slot.className = 'slot ' + (val !== 0 ? 'full' : '');
                slot.innerText = val !== 0 ? val : '';

                if(index === data.in) slot.innerHTML += '<div class="pointer in-ptr">NUEVO ↓</div>';
                if(index === data.out) slot.innerHTML += '<div class="pointer out-ptr">SALE ↑</div>';

                bufferUI.appendChild(slot);
            });

            // 3. Dibujar Hilos con traductor temático para los paneles laterales
            const dibujarHilos = (panelId, hilos) => {
                const panel = document.getElementById(panelId);
                panel.innerHTML = panel.firstElementChild.outerHTML;

                hilos.forEach(hilo => {
                    const card = document.createElement('div');
                    card.className = `tarjeta-hilo estado-${hilo.estado}`;

                    // 3. Dibujar Hilos con traductor temático para los paneles laterales
                    let textoEstado = "";
                    if (hilo.estado === "TRABAJANDO") {
                        textoEstado = hilo.id.includes("Cliente") ? "Mirando Menú..." : "Cocinando...";
                    } else if (hilo.estado === "ESPERANDO") {
                        textoEstado = hilo.id.includes("Cliente") ? "Esperando Espacio" : "Esperando Pedidos";
                    } else if (hilo.estado === "EN_SECCION_CRITICA") {
                        textoEstado = hilo.id.includes("Cliente") ? "Enviando Ticket" : "Tomando Ticket";
                    }

                    card.innerHTML = `
                        <div class="hilo-nombre">${hilo.id}</div>
                        <div class="hilo-estado-texto">${textoEstado}</div>
                    `;
                    panel.appendChild(card);
                });
            };

            if(data.productores) dibujarHilos('panel-productores', data.productores);
            if(data.consumidores) dibujarHilos('panel-consumidores', data.consumidores);
        })
        .catch(err => console.error("Error procesando datos:", err));
}, 200);