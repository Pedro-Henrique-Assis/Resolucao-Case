document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('colaborador-lista')) {
            fetchColaboradores();
    }
});

const API_URL = 'http://localhost:8080/api/v1/colaborador';

async function fetchColaboradores() {
    const loading = document.getElementById('loading');
    const listaContainer = document.getElementById('colaborador-lista');

    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error('Não foi possível carregar os dados da API.');
        }
        const colaboradores = await response.json();

        loading.style.display = 'none';

        if (colaboradores.length === 0) {
            listaContainer.innerHTML = '<p>Nenhum colaborador cadastrado.</p>';
            return;
        }

        colaboradores.forEach(colaborador => {
            const card = document.createElement('a');
            // Atualizei o HTML do card para usar as classes novas
            card.href = `detalhes.html?matricula=${colaborador.matricula}`;
            card.className = 'card'; // Classe CSS atualizada

            card.innerHTML = `
                <div>
                    <h4>${colaborador.nome}</h4>
                    <p class="cargo">${colaborador.cargo}</p>
                </div>
                <div class="card-footer">
                    Ver detalhes &rarr;
                </div>
            `;
            listaContainer.appendChild(card);
        });

    } catch (error) {
        if(loading) loading.style.display = 'none';
        if(listaContainer) listaContainer.innerHTML = `<p style="color: red;">${error.message}</p>`;
        console.error('Erro ao buscar colaboradores:', error);
    }
}