document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const matricula = params.get('matricula');

    if (matricula) {
        fetchDetalhesColaborador(matricula);
    } else {
        window.location.href = 'dashboard.html';
    }
});

const API_URL = 'http://localhost:8080/api/v1/colaborador';
let colaboradorAtual = null; // Armazena dados globais para facilitar edição

async function fetchDetalhesColaborador(matricula) {
    const loading = document.getElementById('loading');

    try {
        const response = await fetch(`${API_URL}/${matricula}`);
        if (!response.ok) throw new Error('Colaborador não encontrado.');

        colaboradorAtual = await response.json();
        loading.style.display = 'none';

        renderInfo(colaboradorAtual);
        renderAvaliacao(colaboradorAtual.avaliacaoComportamento, matricula);
        renderEntregas(colaboradorAtual.entregas, matricula);

    } catch (error) {
        if(loading) loading.innerHTML = `<p style="color: red; text-align:center; padding:2rem;">${error.message}</p>`;
    }
}

// --- RENDERIZAÇÃO PROFISSIONAL (UX/UI) ---

function renderInfo(colaborador) {
    const container = document.getElementById('colaborador-info');
    const dataAdmissao = new Date(colaborador.dataAdmissao).toLocaleDateString('pt-BR', { timeZone: 'UTC' });

    // Aplica a classe específica do header de perfil definida no CSS
    container.className = 'card card-profile-header';

    container.innerHTML = `
        <div class="profile-main">
            <div class="profile-identity">
                <h2>${colaborador.nome}</h2>
                <div class="profile-meta">
                    <div class="meta-item">
                        <span class="meta-icon">&#128188;</span> <span>${colaborador.cargo}</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-icon">&#128197;</span> <span>Admissão: ${dataAdmissao}</span>
                    </div>
                </div>
            </div>

            <div class="action-buttons">
                <button class="btn-icon btn-edit" onclick="editarColaborador()" title="Editar Perfil">
                    &#9998;
                </button>
                <button class="btn-icon btn-delete" onclick="confirmarExclusaoColaborador('${colaborador.matricula}')" title="Excluir Colaborador">
                    &#128465;
                </button>
            </div>
        </div>
    `;
    container.style.display = 'block';
}

function renderAvaliacao(avaliacao, matricula) {
    const container = document.getElementById('avaliacao-container');
    const detalhes = document.getElementById('avaliacao-detalhes');

    // Header da Seção com botões de ação alinhados
    let headerHtml = `
        <div class="section-header">
            <h3><span style="font-size:1.5rem">&#128202;</span> Avaliação Comportamental</h3>
            <div class="action-buttons">
    `;

    if (avaliacao) {
        // Se existe avaliação, mostra botões de Editar e Excluir
        headerHtml += `
            <button class="btn-icon btn-edit" onclick="editarAvaliacao()" title="Editar Avaliação" style="width:36px;height:36px;font-size:1rem;">&#9998;</button>
            <button class="btn-icon btn-delete" onclick="excluirAvaliacao('${matricula}')" title="Excluir Avaliação" style="width:36px;height:36px;font-size:1rem;">&#128465;</button>
        `;
    } else {
        // Se não existe, mostra botão de Cadastro
        headerHtml += `
            <button class="btn-action btn-edit" onclick="window.location.href='form-avaliacao.html'" style="padding: 5px 15px; width: auto; font-size: 0.9rem;">+ Cadastrar Avaliação</button>
        `;
    }
    headerHtml += `</div></div>`;

    // Conteúdo da Avaliação (Grid de Cards)
    let contentHtml = '';
    if (avaliacao) {
        contentHtml = `
            <div class="avaliacao-grid">
                ${criarCardNota('Comportamento', avaliacao.notaAvaliacaoComportamental)}
                ${criarCardNota('Aprendizado', avaliacao.notaAprendizado)}
                ${criarCardNota('Tomada Decisão', avaliacao.notaTomadaDecisao)}
                ${criarCardNota('Autonomia', avaliacao.notaAutonomia)}
            </div>
            <div class="media-container">
                Média Geral: ${avaliacao.mediaNotas.toFixed(2)}
            </div>
        `;
    } else {
        contentHtml = '<p style="text-align:center; color:#999; padding: 2rem;">Nenhuma avaliação comportamental registrada para este colaborador.</p>';
    }

    detalhes.innerHTML = headerHtml + contentHtml;
    container.style.display = 'block';
}

function criarCardNota(titulo, valor) {
    let cor = '#dc3545'; // Ruim (Vermelho)
    if (valor >= 4.0) cor = '#28a745'; // Bom (Verde)
    else if (valor >= 2.5) cor = '#ffc107'; // Médio (Amarelo)

    return `
        <div class="nota-card">
            <span class="nota-titulo">${titulo}</span>
            <div class="nota-badge" style="background-color: ${cor}">${valor.toFixed(1)}</div>
        </div>
    `;
}

function renderEntregas(entregas, matricula) {
    const container = document.getElementById('entregas-container');
    const lista = document.getElementById('entregas-lista');

    // Header da Seção
    let html = `
        <div class="section-header">
            <h3><span style="font-size:1.5rem">&#128230;</span> Entregas Realizadas</h3>
            <button class="btn-action btn-edit" onclick="window.location.href='form-entrega.html'" style="padding: 5px 15px; width: auto; font-size: 0.9rem; background: var(--cor-primaria)">+ Nova Entrega</button>
        </div>
        <div>
    `;

    if (!entregas || entregas.length === 0) {
        html += '<p style="text-align:center; color:#999; padding: 2rem;">Nenhuma entrega registrada.</p>';
    } else {
        entregas.forEach(entrega => {
            let corNota = '#dc3545';
            if (entrega.nota >= 4.0) corNota = '#28a745';
            else if (entrega.nota >= 2.5) corNota = '#ffc107';

            html += `
                <div class="entrega-card">
                    <div class="entrega-content">
                        <h4>${entrega.descricao}</h4>
                        <span class="entrega-nota" style="background-color: ${corNota}">Nota: ${entrega.nota.toFixed(1)}</span>
                    </div>
                    <div class="action-buttons">
                        <button class="btn-icon btn-edit" onclick="editarEntrega('${matricula}', ${entrega.id}, '${entrega.descricao}', ${entrega.nota})" style="width:32px;height:32px;font-size:0.9rem;" title="Editar">
                            &#9998;
                        </button>
                        <button class="btn-icon btn-delete" onclick="excluirEntrega('${matricula}', ${entrega.id})" style="width:32px;height:32px;font-size:0.9rem;" title="Excluir">
                            &#128465;
                        </button>
                    </div>
                </div>
            `;
        });
    }
    html += `</div>`;
    lista.innerHTML = html;
    container.style.display = 'block';
}

// --- FUNÇÕES DE EXCLUSÃO (DELETE) ---

async function confirmarExclusaoColaborador(matricula) {
    const result = await Swal.fire({
        title: 'Tem certeza?',
        text: "Isso apagará o colaborador e todas as suas entregas e avaliações permanentemente!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Sim, excluir!',
        cancelButtonText: 'Cancelar'
    });

    if (result.isConfirmed) {
        try {
            const res = await fetch(`${API_URL}/${matricula}`, { method: 'DELETE' });
            if (res.ok) {
                await Swal.fire('Excluído!', 'Colaborador removido com sucesso.', 'success');
                window.location.href = 'dashboard.html';
            } else {
                throw new Error('Erro ao excluir.');
            }
        } catch (error) {
            Swal.fire('Erro', 'Não foi possível excluir o colaborador.', 'error');
        }
    }
}

async function excluirAvaliacao(matricula) {
    const result = await Swal.fire({
        title: 'Excluir Avaliação?',
        text: "As notas comportamentais serão removidas.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        confirmButtonText: 'Sim, remover'
    });

    if (result.isConfirmed) {
        await realizarRequisicao(`${API_URL}/${matricula}/avaliacao`, 'DELETE', 'Avaliação removida.');
    }
}

async function excluirEntrega(matricula, id) {
    const result = await Swal.fire({
        title: 'Excluir Entrega?',
        text: "Essa ação não pode ser desfeita.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        confirmButtonText: 'Sim, excluir'
    });

    if (result.isConfirmed) {
        await realizarRequisicao(`${API_URL}/${matricula}/entrega/${id}`, 'DELETE', 'Entrega removida.');
    }
}

// --- FUNÇÕES DE EDIÇÃO (UPDATE/PATCH) ---

async function editarColaborador() {
    const { value: formValues } = await Swal.fire({
        title: 'Editar Colaborador',
        html: `
            <label class="swal2-input-label">Nome</label>
            <input id="swal-nome" class="swal2-input" value="${colaboradorAtual.nome}">

            <label class="swal2-input-label">Cargo</label>
            <input id="swal-cargo" class="swal2-input" value="${colaboradorAtual.cargo}">

            <label class="swal2-input-label">Data Admissão</label>
            <input id="swal-data" type="date" class="swal2-input" value="${colaboradorAtual.dataAdmissao}">
        `,
        focusConfirm: false,
        showCancelButton: true,
        preConfirm: () => {
            return {
                nome: document.getElementById('swal-nome').value,
                cargo: document.getElementById('swal-cargo').value,
                dataAdmissao: document.getElementById('swal-data').value
            }
        }
    });

    if (formValues) {
        await realizarRequisicao(`${API_URL}/${colaboradorAtual.matricula}`, 'PATCH', 'Colaborador atualizado!', formValues);
    }
}

async function editarAvaliacao() {
    const av = colaboradorAtual.avaliacaoComportamento;

    const { value: formValues } = await Swal.fire({
        title: 'Editar Notas',
        html: `
            <label class="swal2-input-label">Comportamento</label>
            <input id="edit-comp" type="number" step="0.1" min="1" max="5" class="swal2-input" value="${av.notaAvaliacaoComportamental}">

            <label class="swal2-input-label">Aprendizado</label>
            <input id="edit-apren" type="number" step="0.1" min="1" max="5" class="swal2-input" value="${av.notaAprendizado}">

            <label class="swal2-input-label">Tomada de Decisão</label>
            <input id="edit-decis" type="number" step="0.1" min="1" max="5" class="swal2-input" value="${av.notaTomadaDecisao}">

            <label class="swal2-input-label">Autonomia</label>
            <input id="edit-auto" type="number" step="0.1" min="1" max="5" class="swal2-input" value="${av.notaAutonomia}">
        `,
        focusConfirm: false,
        showCancelButton: true,
        preConfirm: () => {
            return {
                notaAvaliacaoComportamental: document.getElementById('edit-comp').value,
                notaAprendizado: document.getElementById('edit-apren').value,
                notaTomadaDecisao: document.getElementById('edit-decis').value,
                notaAutonomia: document.getElementById('edit-auto').value
            }
        }
    });

    if (formValues) {
        await realizarRequisicao(`${API_URL}/${colaboradorAtual.matricula}/avaliacao`, 'PATCH', 'Notas atualizadas!', formValues);
    }
}

async function editarEntrega(matricula, id, descricaoAtual, notaAtual) {
    const { value: formValues } = await Swal.fire({
        title: 'Editar Entrega',
        html: `
            <label class="swal2-input-label">Descrição</label>
            <input id="edit-desc" class="swal2-input" value="${descricaoAtual}">

            <label class="swal2-input-label">Nota</label>
            <input id="edit-nota" type="number" step="0.1" min="1" max="5" class="swal2-input" value="${notaAtual}">
        `,
        focusConfirm: false,
        showCancelButton: true,
        preConfirm: () => {
            return {
                descricao: document.getElementById('edit-desc').value,
                nota: document.getElementById('edit-nota').value
            }
        }
    });

    if (formValues) {
        await realizarRequisicao(`${API_URL}/${matricula}/entrega/${id}`, 'PATCH', 'Entrega atualizada!', formValues);
    }
}

// --- HELPER GENÉRICO PARA REQUISIÇÕES ---
async function realizarRequisicao(url, method, successMsg, bodyData = null) {
    try {
        const options = {
            method: method,
            headers: { 'Content-Type': 'application/json' }
        };

        if (bodyData) {
            options.body = JSON.stringify(bodyData);
        }

        const response = await fetch(url, options);

        if (response.ok || response.status === 204) {
            await Swal.fire('Sucesso!', successMsg, 'success');
            location.reload(); // Recarrega para mostrar dados novos
        } else {
            const error = await response.json().catch(() => ({}));
            throw new Error(error.message || 'Erro na operação.');
        }
    } catch (e) {
        Swal.fire('Erro!', e.message, 'error');
    }
}