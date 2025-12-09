import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class Servidor {

    private static Connection con;

    public static void main(String[] args) throws Exception {

        // Conectar ao SQLite
        con = DriverManager.getConnection("jdbc:sqlite:academiflow.db");

        // Criar tabelas
        criarTabelas();

        // Inserir dados de exemplo
        inserirDadosIniciais();

        // Criar servidor HTTP
        HttpServer s = HttpServer.create(new InetSocketAddress(8082), 0);

        // Rotas
        s.createContext("/", t -> enviar(t, "login.html"));
        s.createContext("/creditos", t -> enviar(t, "creditos.html"));
        s.createContext("/login", Servidor::login);
        s.createContext("/professor", Servidor::professor);
        s.createContext("/aluno", Servidor::aluno);
        s.createContext("/cadastrar", Servidor::cadastrarAtividade);
        s.createContext("/deletar", Servidor::deletarAtividade);
        s.createContext("/confirmar", Servidor::confirmarParticipacao);
        s.createContext("/css/estilo.css", t -> enviarCSS(t, "/css/estilo.css"));
        s.createContext("/css/login.css", t -> enviarCSS(t, "/css/login.css"));
        s.createContext("/css/creditos.css", t -> enviarCSS(t, "/css/creditos.css"));
        // Rotas para imagens
        s.createContext("/img/felipec.jpg", t -> enviarImagem(t, "img/felipec.jpg"));
        s.createContext("/img/felipes.jpg", t -> enviarImagem(t, "img/felipes.jpg"));
        s.createContext("/img/giovanna.jpg", t -> enviarImagem(t, "img/giovanna.jpg"));
        s.createContext("/img/otavio.jpg", t -> enviarImagem(t, "img/otavio.jpg"));
        s.createContext("/img/login.jpg", t -> enviarImagem(t, "img/login.jpg"));
        s.createContext("/img/body.jpg", t -> enviarImagem(t, "img/body.jpg"));

        s.start();
        System.out.println("Servidor rodando em http://localhost:8082/");
    }

    // -------------------- CRIAR TABELAS --------------------

    private static void criarTabelas() throws SQLException {
        
        // Tabela usuarios
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT UNIQUE," +
                "senha TEXT," +
                "tipo TEXT" +
                ")";
        con.createStatement().execute(sqlUsuarios);

        // Tabela atividades
        String sqlAtividades = "CREATE TABLE IF NOT EXISTS atividades (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT,"     +
                "titulo TEXT," +
                "descricao TEXT," +
                "data TEXT," +
                "horario TEXT," +
                "professor TEXT" +
                ")";
        con.createStatement().execute(sqlAtividades);

        // Tabela participacoes
        String sqlParticipacoes = "CREATE TABLE IF NOT EXISTS participacoes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "atividade_id INTEGER," +
                "aluno TEXT," +
                "confirmado TEXT," +
                "FOREIGN KEY(atividade_id) REFERENCES atividades(id)" +
                ")";
        con.createStatement().execute(sqlParticipacoes);
    }

    // -------------------- INSERIR DADOS INICIAIS --------------------

    private static void inserirDadosIniciais() throws SQLException {
        
        // Verificar se já existem usuários
        ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) as total FROM usuarios");
        if (rs.next() && rs.getInt("total") > 0) {
            return; // Já tem dados
        }

        // Inserir professores
        inserirUsuario("prof1", "123", "professor");
        inserirUsuario("prof2", "123", "professor");

        // Inserir alunos
        inserirUsuario("aluno1", "123", "aluno");
        inserirUsuario("aluno2", "123", "aluno");
        inserirUsuario("aluno3", "123", "aluno");

        System.out.println("Dados iniciais inseridos!");
        System.out.println("Professores: prof1/123, prof2/123");
        System.out.println("Alunos: aluno1/123, aluno2/123, aluno3/123");
    }

    private static void inserirUsuario(String nome, String senha, String tipo) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO usuarios (nome, senha, tipo) VALUES (?, ?, ?)"
        );
        ps.setString(1, nome);
        ps.setString(2, senha);
        ps.setString(3, tipo);
        ps.executeUpdate();
    }

    // -------------------- LOGIN --------------------

    private static void login(HttpExchange t) throws IOException {
        
        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            redirecionar(t, "/");
            return;
        }

        String corpo = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);
        
        String usuario = pega(corpo, "usuario");
        String senha = pega(corpo, "senha");
        String tipo = pega(corpo, "tipo");

        try {
            // Verificar credenciais
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM usuarios WHERE nome = ? AND senha = ? AND tipo = ?"
            );
            ps.setString(1, usuario);
            ps.setString(2, senha);
            ps.setString(3, tipo);
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // Login válido
                if ("professor".equals(tipo)) {
                    redirecionar(t, "/professor?usuario=" + usuario);
                } else {
                    redirecionar(t, "/aluno?usuario=" + usuario);
                }
            } else {
                // Login inválido
                enviarMensagem(t, "Login inválido! Verifique usuário, senha e tipo.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            enviarMensagem(t, "Erro ao processar login.");
        }
    }

    // -------------------- PROFESSOR --------------------

    private static void professor(HttpExchange t) throws IOException {
        
        String query = t.getRequestURI().getQuery();
        String usuario = "";
        
        if (query != null) {
            usuario = pega(query, "usuario");
        }

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>AcademiFlow - Professor</title>");
        html.append("<link rel=\"stylesheet\" href=\"/css/estilo.css\">");
        html.append("</head><body>");

        html.append("<div class=\"header\">");
        html.append("<h1>AcademiFlow - Professor</h1>");
        html.append("<p>Bem-vindo, <strong>").append(usuario).append("</strong></p>");
        html.append("</div>");

        // Formulário de cadastro
        html.append("<div class=\"container\">");
        html.append("<h2>Cadastrar Nova Atividade</h2>");
        html.append("<form method=\"POST\" action=\"/cadastrar?usuario=").append(usuario).append("\">");
        html.append("<input type=\"text\" name=\"titulo\" placeholder=\"Título da atividade\" required>");
        html.append("<textarea name=\"descricao\" placeholder=\"Descrição\" required></textarea>");
        html.append("<input type=\"date\" name=\"data\" required>");
        html.append("<input type=\"time\" name=\"horario\" required>");
        html.append("<button type=\"submit\">Cadastrar Atividade</button>");
        html.append("</form>");
        html.append("</div>");

        // Listar atividades do professor
        html.append("<div class=\"container\">");
        html.append("<h2>Minhas Atividades</h2>");

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM atividades WHERE professor = ? ORDER BY data DESC, horario DESC")) {
            
            ps.setString(1, usuario);
            ResultSet rs = ps.executeQuery();

            boolean temAtividades = false;

            while (rs.next()) {
                temAtividades = true;
                
                int id = rs.getInt("id");
                String titulo = rs.getString("titulo");
                String descricao = rs.getString("descricao");
                String data = rs.getString("data");
                String horario = rs.getString("horario");

                // Contar confirmações
                int confirmados = contarConfirmacoes(id);

                html.append("<div class=\"card\">");
                html.append("<h3>").append(titulo).append("</h3>");
                html.append("<p><strong>Data:</strong> ").append(data).append("</p>");
                html.append("<p><strong>Horário:</strong> ").append(horario).append("</p>");
                html.append("<p><strong>Descrição:</strong> ").append(descricao).append("</p>");
                html.append("<p class=\"confirmacoes\">").append(confirmados).append(" aluno(s) confirmado(s)</p>");
                
                // Botão deletar
                html.append("<form method=\"POST\" action=\"/deletar?usuario=").append(usuario).append("\">");
                html.append("<input type=\"hidden\" name=\"id\" value=\"").append(id).append("\">");
                html.append("<button type=\"submit\" class=\"btn-deletar\">Deletar Atividade</button>");
                html.append("</form>");
                
                html.append("</div>");
            }

            if (!temAtividades) {
                html.append("<p class=\"vazio\">Nenhuma atividade cadastrada ainda.</p>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            html.append("<p>Erro ao carregar atividades.</p>");
        }

        html.append("</div>");
        html.append("</body></html>");

        enviarHTML(t, html.toString());
    }

    // -------------------- CADASTRAR ATIVIDADE --------------------

    private static void cadastrarAtividade(HttpExchange t) throws IOException {
        
        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            redirecionar(t, "/professor");
            return;
        }

        String query = t.getRequestURI().getQuery();
        String usuario = pega(query, "usuario");
        
        String corpo = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);
        
        String titulo = pega(corpo, "titulo");
        String descricao = pega(corpo, "descricao");
        String data = pega(corpo, "data");
        String horario = pega(corpo, "horario");

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO atividades (titulo, descricao, data, horario, professor) VALUES (?,?,?,?,?)")) {
            
            ps.setString(1, titulo);
            ps.setString(2, descricao);
            ps.setString(3, data);
            ps.setString(4, horario);
            ps.setString(5, usuario);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        redirecionar(t, "/professor?usuario=" + usuario);
    }

    // -------------------- DELETAR ATIVIDADE --------------------

    private static void deletarAtividade(HttpExchange t) throws IOException {
        
        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            redirecionar(t, "/professor");
            return;
        }

        String query = t.getRequestURI().getQuery();
        String usuario = pega(query, "usuario");
        
        String corpo = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);
        String idStr = pega(corpo, "id");

        try {
            int id = Integer.parseInt(idStr);

            // Deletar participações primeiro
            PreparedStatement ps1 = con.prepareStatement("DELETE FROM participacoes WHERE atividade_id = ?");
            ps1.setInt(1, id);
            ps1.executeUpdate();

            // Deletar atividade
            PreparedStatement ps2 = con.prepareStatement("DELETE FROM atividades WHERE id = ?");
            ps2.setInt(1, id);
            ps2.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        redirecionar(t, "/professor?usuario=" + usuario);
    }

    // -------------------- ALUNO --------------------

    private static void aluno(HttpExchange t) throws IOException {
        
        String query = t.getRequestURI().getQuery();
        String usuario = "";
        
        if (query != null) {
            usuario = pega(query, "usuario");
        }

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>AcademiFlow - Aluno</title>");
        html.append("<link rel=\"stylesheet\" href=\"/css/estilo.css\">");
        html.append("</head><body>");

        html.append("<div class=\"header\">");
        html.append("<h1>AcademiFlow - Aluno</h1>");
        html.append("<p>Bem-vindo, <strong>").append(usuario).append("</strong></p>");
        html.append("</div>");

        html.append("<div class=\"container\">");
        html.append("<h2>Atividades Disponíveis</h2>");

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM atividades ORDER BY data DESC, horario DESC")) {

            boolean temAtividades = false;

            while (rs.next()) {
                temAtividades = true;
                
                int id = rs.getInt("id");
                String titulo = rs.getString("titulo");
                String descricao = rs.getString("descricao");
                String data = rs.getString("data");
                String horario = rs.getString("horario");
                String professor = rs.getString("professor");

                // Verificar se aluno já confirmou
                String statusParticipacao = verificarParticipacao(id, usuario);

                String classeCard = "card";
                if ("sim".equals(statusParticipacao)) {
                    classeCard = "card card-confirmado";
                } else if ("nao".equals(statusParticipacao)) {
                    classeCard = "card card-nao-confirmado";
                }

                html.append("<div class=\"").append(classeCard).append("\">");
                html.append("<h3>").append(titulo).append("</h3>");
                html.append("<p><strong>Data:</strong> ").append(data).append("</p>");
                html.append("<p><strong>Horário:</strong> ").append(horario).append("</p>");
                html.append("<p><strong>Professor:</strong> ").append(professor).append("</p>");
                html.append("<p><strong>Descrição:</strong> ").append(descricao).append("</p>");

                if (statusParticipacao != null) {
                    html.append("<p class=\"status\">Status: ");
                    html.append("sim".equals(statusParticipacao) ? "Vou participar ✓" : "Não vou participar ✗");
                    html.append("</p>");
                }

                // Botões de confirmação
                html.append("<div class=\"botoes\">");
                
                html.append("<form method=\"POST\" action=\"/confirmar?usuario=").append(usuario).append("\">");
                html.append("<input type=\"hidden\" name=\"atividade_id\" value=\"").append(id).append("\">");
                html.append("<input type=\"hidden\" name=\"confirmado\" value=\"sim\">");
                html.append("<button type=\"submit\" class=\"btn-sim\">Vou Participar</button>");
                html.append("</form>");

                html.append("<form method=\"POST\" action=\"/confirmar?usuario=").append(usuario).append("\">");
                html.append("<input type=\"hidden\" name=\"atividade_id\" value=\"").append(id).append("\">");
                html.append("<input type=\"hidden\" name=\"confirmado\" value=\"nao\">");
                html.append("<button type=\"submit\" class=\"btn-nao\">Não Vou</button>");
                html.append("</form>");

                html.append("</div>");
                html.append("</div>");
            }

            if (!temAtividades) {
                html.append("<p class=\"vazio\">Nenhuma atividade disponível.</p>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            html.append("<p>Erro ao carregar atividades.</p>");
        }

        html.append("</div>");
        html.append("</body></html>");

        enviarHTML(t, html.toString());
    }

    // -------------------- CONFIRMAR PARTICIPAÇÃO --------------------

    private static void confirmarParticipacao(HttpExchange t) throws IOException {
        
        if (!t.getRequestMethod().equalsIgnoreCase("POST")) {
            redirecionar(t, "/aluno");
            return;
        }

        String query = t.getRequestURI().getQuery();
        String usuario = pega(query, "usuario");
        
        String corpo = URLDecoder.decode(ler(t), StandardCharsets.UTF_8);
        
        String atividadeIdStr = pega(corpo, "atividade_id");
        String confirmado = pega(corpo, "confirmado");

        try {
            int atividadeId = Integer.parseInt(atividadeIdStr);

            // Verificar se já existe registro
            PreparedStatement psCheck = con.prepareStatement(
                "SELECT id FROM participacoes WHERE atividade_id = ? AND aluno = ?"
            );
            psCheck.setInt(1, atividadeId);
            psCheck.setString(2, usuario);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                // Atualizar
                PreparedStatement psUpdate = con.prepareStatement(
                    "UPDATE participacoes SET confirmado = ? WHERE atividade_id = ? AND aluno = ?"
                );
                psUpdate.setString(1, confirmado);
                psUpdate.setInt(2, atividadeId);
                psUpdate.setString(3, usuario);
                psUpdate.executeUpdate();
            } else {
                // Inserir
                PreparedStatement psInsert = con.prepareStatement(
                    "INSERT INTO participacoes (atividade_id, aluno, confirmado) VALUES (?,?,?)"
                );
                psInsert.setInt(1, atividadeId);
                psInsert.setString(2, usuario);
                psInsert.setString(3, confirmado);
                psInsert.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        redirecionar(t, "/aluno?usuario=" + usuario);
    }

    // -------------------- FUNÇÕES AUXILIARES --------------------

    private static String verificarParticipacao(int atividadeId, String aluno) {
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT confirmado FROM participacoes WHERE atividade_id = ? AND aluno = ?"
            );
            ps.setInt(1, atividadeId);
            ps.setString(2, aluno);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("confirmado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int contarConfirmacoes(int atividadeId) {
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) as total FROM participacoes WHERE atividade_id = ? AND confirmado = 'sim'"
            );
            ps.setInt(1, atividadeId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static String pega(String corpo, String campo) {
        for (String s : corpo.split("&")) {
            String[] p = s.split("=", 2);
            if (p.length == 2 && p[0].equals(campo)) return p[1];
        }
        return "";
    }

    private static String ler(HttpExchange t) throws IOException {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8)
        );
        String linha = br.readLine();
        return (linha == null) ? "" : linha;
    }

    private static void enviar(HttpExchange t, String arq) throws IOException {
        File f = new File("src/main/java/" + arq);
        byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    private static void enviarCSS(HttpExchange t, String arq) throws IOException {
        File f = new File("src/main/java/" + arq);
        byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "text/css; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    private static void enviarHTML(HttpExchange t, String html) throws IOException {
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }

    private static void enviarImagem(HttpExchange t, String arq) throws IOException {
        File f = new File("src/main/java/" + arq);
        byte[] b = java.nio.file.Files.readAllBytes(f.toPath());
        t.getResponseHeaders().add("Content-Type", "image/jpeg");
        t.sendResponseHeaders(200, b.length);
        t.getResponseBody().write(b);
        t.close();
    }



    private static void enviarMensagem(HttpExchange t, String mensagem) throws IOException {
        String html = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Erro</title>" +
                      "<link rel=\"stylesheet\" href=\"/css/estilo.css\"></head><body>" +
                      "<div class=\"container\"><h2>Atenção</h2><p>" + mensagem + "</p>" +
                      "<a href=\"/\">Voltar ao Login</a></div></body></html>";
        enviarHTML(t, html);
    }

    private static void redirecionar(HttpExchange t, String rota) throws IOException {
        t.getResponseHeaders().add("Location", rota);
        t.sendResponseHeaders(302, -1);
        t.close();
    }
}
