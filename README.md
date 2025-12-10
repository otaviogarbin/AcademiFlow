# 🎓 AcademiFlow — Sistema de Atividades Escolares

![Status](https://img.shields.io/badge/Status-Ativo-brightgreen?style=for-the-badge)
![Versão](https://img.shields.io/badge/Versão-1.0-blue?style=for-the-badge)
![Java](https://img.shields.io/badge/Java--red?style=for-the-badge\&logo=java)
![Maven](https://img.shields.io/badge/Maven-Build-orange?style=for-the-badge\&logo=apachemaven)
![Plataforma](https://img.shields.io/badge/Plataforma-Web-9cf?style=for-the-badge)

---

# 📘 O que é o AcademiFlow?

O **AcademiFlow** é um sistema web simples e eficiente que ajuda escolas a organizar atividades acadêmicas.

👨‍🏫 **Professores:** Criam atividades e acompanham confirmações
🎓 **Alunos:** Visualizam atividades e confirmam presença
📚 **Escolas:** Ganham organização e centralização

---

# ❗ Problema Resolvido

Antes do AcademiFlow:

* ❌ Atividades enviadas por papel, WhatsApp ou e-mail
* ❌ Alunos perdiam datas e horários
* ❌ Nenhuma confirmação de participação
* ❌ Falta de controle e organização

Com o AcademiFlow:

✨ Tudo centralizado
✨ Fácil de usar
✨ Professores organizados
✨ Alunos informados
✨ Gestão simplificada

---

# ⚙️ Como Funciona

```
NAVEGADOR
↓
SERVIDOR JAVA
↓
BANCO DE DADOS SQLite
```

✔️ Simples
✔️ Direto
✔️ Funcional

---

# 🛠️ Tecnologias Utilizadas

| Tecnologia     | Descrição            |
| -------------- | -------------------- |
| **Java**   | Backend do sistema   |
| **Maven**      | Build e dependências |
| **HTML/CSS**   | Interface            |
| **SQLite**     | Banco de dados       |
| **HTTP/HTTPS** | Comunicação          |

---

# 🗃️ Banco de Dados

### 👤 Usuários

```
Usuário | Senha | Tipo
professor1 | 1234 | Professor
aluno1     | 1234 | Aluno
```

### 📅 Atividades

```
ID | Título | Data | Hora | Professor
1  | Aula de Matemática | 20/12/2025 | 14:00 | professor1
```

### 📝 Confirmações

```
Atividade 1 → aluno1 = SIM
Atividade 1 → aluno2 = NÃO
```


---

# 🧪 Testes Rápidos

### ✔️ Teste Login

Professor → `professor1` : `1234`
Aluno → `aluno1` : `1234`

### ✔️ Criar atividade

Professor cria → aparece no aluno

### ✔️ Confirmar presença

Aluno clica em “Sim” → fica verde

---

# 📁 Estrutura de Pastas

```
AcademiFlow/
├── README.md
├── pom.xml
├── academiflow.db
└── src/main/
    ├── java/Servidor.java
    └── resources/
        ├── html/
        ├── css/
        └── img/
```

---

# 👥 Créditos

👤 **Otávio Garbin**
👤 **Felipe Santos**
👤 **Felipe Chagas**
👤 **Giovanna Alves**

---

# 🔮 Melhorias Futuras

* 🔐 Criptografar senhas
* 📝 Editar atividades
* 🔎 Filtrar por data
* ✉️ Enviar notificações
* 🌙 Tema escuro
* ♿ Melhor acessibilidade

---

# 🆘 Problemas Comuns

### ❗ Porta 8082 usada

→ Feche aplicativos ou reinicie

### ❗ Java não encontrado

→ Instalar: [https://www.oracle.com/java/](https://www.oracle.com/java/)

### ❗ Maven não encontrado

→ Instalar: [https://maven.apache.org/](https://maven.apache.org/)

---

# 📌 Resumo Final

* 🚀 Simples
* 📚 Útil
* 🏫 Perfeito para escolas
* 💾 Banco local
* 🔧 Fácil de executar

---
