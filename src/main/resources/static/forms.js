const API_BASE_URL = 'http://localhost:8080/api/v1/colaborador';

document.addEventListener('DOMContentLoaded', () => {

    // Handler para Cadastro de Colaborador
    const formColaborador = document.getElementById('form-colaborador');
    if (formColaborador) {
        formColaborador.addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = {
                nome: document.getElementById('nome').value,
                cargo: document.getElementById('cargo').value,
                dataAdmissao: document.getElementById('dataAdmissao').value
            };
            await sendData(API_BASE_URL, 'POST', data, 'Colaborador cadastrado com sucesso!');
        });
    }

    // Handler para Cadastro de Avaliação
    const formAvaliacao = document.getElementById('form-avaliacao');
    if (formAvaliacao) {
        formAvaliacao.addEventListener('submit', async (e) => {
            e.preventDefault();
            const matricula = document.getElementById('matricula').value.trim();

            const data = {
                notaAvaliacaoComportamental: parseFloat(document.getElementById('notaComportamental').value),
                notaAprendizado: parseFloat(document.getElementById('notaAprendizado').value),
                notaTomadaDecisao: parseFloat(document.getElementById('notaDecisao').value),
                notaAutonomia: parseFloat(document.getElementById('notaAutonomia').value)
            };

            // Endpoint: /api/v1/colaborador/{matricula}/avaliacao
            const url = `${API_BASE_URL}/${matricula}/avaliacao`;
            await sendData(url, 'POST', data, 'Avaliação registrada com sucesso!');
        });
    }

    // Handler para Cadastro de Entrega
    const formEntrega = document.getElementById('form-entrega');
    if (formEntrega) {
        formEntrega.addEventListener('submit', async (e) => {
            e.preventDefault();
            const matricula = document.getElementById('matricula').value.trim();

            const data = {
                descricao: document.getElementById('descricao').value,
                nota: parseFloat(document.getElementById('nota').value)
            };

            // Endpoint: /api/v1/colaborador/{matricula}/entrega
            const url = `${API_BASE_URL}/${matricula}/entrega`;
            await sendData(url, 'POST', data, 'Entrega registrada com sucesso!');
        });
    }
});

// Função genérica de envio
async function sendData(url, method, data, successMsg) {
    const feedback = document.getElementById('feedback');
    const submitBtn = document.querySelector('.btn-submit');

    // Feedback visual de carregamento
    submitBtn.disabled = true;
    submitBtn.textContent = 'Enviando...';
    feedback.style.display = 'none';

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            showFeedback(successMsg, 'success');
            // Limpa o formulário
            document.querySelector('form').reset();
        } else {
            // Tenta pegar mensagem de erro da API ou usa genérica
            const errorData = await response.json().catch(() => ({}));
            const msg = errorData.message || 'Erro ao processar a requisição. Verifique os dados.';
            showFeedback(msg, 'error');
        }
    } catch (error) {
        console.error(error);
        showFeedback('Erro de conexão com o servidor.', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = submitBtn.getAttribute('data-original-text') || 'Salvar / Cadastrar';
    }
}

function showFeedback(message, type) {
    const feedback = document.getElementById('feedback');
    feedback.textContent = message;
    feedback.className = `feedback-message feedback-${type}`;
    feedback.style.display = 'block';
}