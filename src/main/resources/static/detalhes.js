document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const matricula = params.get('matricula');

    if (matricula) {
        fetchDetalhesColaborador(matricula);
    } else {
        window.location.href = 'index.html'; // Redireciona se não houver matrícula
    }
});

const API_URL = 'http://localhost:8080/api/v1/colaborador';

async function fetchDetalhesColaborador(matricula) {
    const loading = document.getElementById('loading');

    try {
        const response = await fetch(`${API_URL}/${matricula}`);
        if (!response.ok) {
            throw new Error('Colaborador não encontrado.');
        }
        const colaborador = await response.json();

        loading.style.display = 'none';

        renderInfo(colaborador);
        renderAvaliacao(colaborador.avaliacaoComportamento);
        renderEntregas(colaborador.entregas);

    } catch (error) {
        loading.style.display = 'none';
        document.body.innerHTML += `<p style="color: red; text-align: center;">${error.message}</p>`;
        console.error('Erro ao buscar detalhes:', error);
    }
}

function renderInfo(colaborador) {
    const container = document.getElementById('colaborador-info');
    const dataAdmissao = new Date(colaborador.dataAdmissao).toLocaleDateString('pt-BR', { timeZone: 'UTC' });

    container.innerHTML = `
        <h2>${colaborador.nome}</h2>
        <p><strong>Cargo:</strong> ${colaborador.cargo}</p>
        <p><strong>Data de Admissão:</strong> ${dataAdmissao}</p>
    `;
    container.style.display = 'block';
}

function renderAvaliacao(avaliacao) {
    const container = document.getElementById('avaliacao-container');
    const detalhes = document.getElementById('avaliacao-detalhes');

    if (!avaliacao) {
        detalhes.innerHTML = '<p>Nenhuma avaliação comportamental cadastrada.</p>';
        container.style.display = 'block';
        return;
    }

    detalhes.innerHTML = `
        <ul>
            <li>
                <span class="label">Comportamento</span>
                <span class="nota-valor" style="background-color: ${getCorNota(avaliacao.notaAvaliacaoComportamental)}">
                    ${avaliacao.notaAvaliacaoComportamental.toFixed(1)}
                </span>
            </li>
            <li>
                <span class="label">Aprendizado</span>
                <span class="nota-valor" style="background-color: ${getCorNota(avaliacao.notaAprendizado)}">
                    ${avaliacao.notaAprendizado.toFixed(1)}
                </span>
            </li>
            <li>
                <span class="label">Tomada de Decisão</span>
                <span class="nota-valor" style="background-color: ${getCorNota(avaliacao.notaTomadaDecisao)}">
                    ${avaliacao.notaTomadaDecisao.toFixed(1)}
                </span>
            </li>
            <li>
                <span class="label">Autonomia</span>
                <span class="nota-valor" style="background-color: ${getCorNota(avaliacao.notaAutonomia)}">
                    ${avaliacao.notaAutonomia.toFixed(1)}
                </span>
            </li>
        </ul>
        <div class="media-final">
            Média Comportamental: ${avaliacao.mediaNotas.toFixed(2)}
        </div>
    `;
    container.style.display = 'block';
}

function renderEntregas(entregas) {
    const container = document.getElementById('entregas-container');
    const lista = document.getElementById('entregas-lista');

    if (!entregas || entregas.length === 0) {
        lista.innerHTML = '<p>Nenhuma entrega cadastrada.</p>';
        container.style.display = 'block';
        return;
    }

    entregas.forEach(entrega => {
        const item = document.createElement('div');
        item.className = 'entrega-item';
        item.innerHTML = `
            <p>${entrega.descricao}</p>
            <span class="nota-valor" style="background-color: ${getCorNota(entrega.nota)}">
                ${entrega.nota.toFixed(1)}
            </span>
        `;
        lista.appendChild(item);
    });
    container.style.display = 'block';
}

function getCorNota(nota) {
    if (nota >= 4.0) return 'var(--cor-nota-boa)';
    if (nota >= 2.5) return 'var(--cor-nota-media)';
    return 'var(--cor-nota-ruim)';
}