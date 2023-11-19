package sistemaCaptura;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import sistemaCaptura.conexao.Conexao;
import sistemaCaptura.user.*;

import java.util.List;
import java.util.Scanner;

public class AppHistorico {

    public static void main(String[] args) {
        Conexao conexao = new Conexao();
        JdbcTemplate con = conexao.getConexaoDoBanco();
        HistConsmRecurso histConsmRecurso = new HistConsmRecurso();

        Scanner in = new Scanner(System.in);
        Integer escolha;

        do {
            System.out.println("-".repeat(15));
            System.out.println("Bem-vindo ao sistema Nowl-Will Dantas");
            System.out.println("Escolha uma das opções abaixo");
            System.out.println("1 - Fazer login");
            System.out.println("2 - Sair");
            System.out.println("3 - Visualizar e Editar Perfil");
            System.out.println("4 - Sugestões");
            System.out.println("5 - Cadastrar maquina");
            System.out.println("-".repeat(15));

            escolha = in.nextInt();

            switch (escolha) {
                case 1:
                    fazerLogin(con, histConsmRecurso, in);
                    break;
                case 2:
                    exibirMensagemDespedida();
                    histConsmRecurso.fecharSistema();
                    break;
                case 3:
                    visualizarEditarPerfil(con);
                    break;
                case 4:
                    enviarSugestao(con, in);
                    break;
                case 5:
                    cadastrarMaquina(con);
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        } while (escolha != 5);
    }

    private static void fazerLogin(JdbcTemplate con, HistConsmRecurso histConsmRecurso, Scanner in) {
        Scanner leitor = new Scanner(System.in);
        Integer numeroMaquina = null;
        Usuario usuario;

        System.out.println("Digite o seu email:");
        String email = leitor.nextLine();
        while (!email.contains("@")) {
            System.out.println("O email deve conter o caractere '@'. Tente novamente:");
            email = leitor.nextLine();
        }

        System.out.println("Digite a sua senha (mínimo de 8 caracteres):");
        String senha = leitor.nextLine();
        while (senha.length() < 8) {
            System.out.println("A senha deve ter no mínimo 8 caracteres. Tente novamente:");
            senha = leitor.nextLine();
        }

        List<Usuario> usuarios = con.query("SELECT * FROM usuario WHERE email = ? AND senha = ?",
                new BeanPropertyRowMapper<>(Usuario.class), email, senha);

        if (usuarios.size() > 0) {
            if (usuarios.get(0).getFkTipoUsuario().equals(1)) {
                usuario = new AdmNowl(usuarios.get(0));
                System.out.println("Bem-vindo ADM Nowl " + usuario.getNome());
            } else if (usuarios.get(0).getFkTipoUsuario().equals(2)) {
                usuario = new Adiministrador(usuarios.get(0));
                System.out.println("Bem-vindo ADM " + usuario.getNome());
            } else if (usuarios.get(0).getFkTipoUsuario().equals(3)) {
                usuario = new Professor(usuarios.get(0));
                System.out.println("Bem-vindo Professor " + usuario.getNome());
            } else {
                usuario = new Aluno(usuarios.get(0));
                System.out.println("Bem-vindo Usuário " + usuario.getNome());
            }

            Integer opcaoUsuario;

            do {
                System.out.println("-".repeat(15));

                if (usuario instanceof AdmNowl) {
                    System.out.println("Opções de ADM Nowl");
                    System.out.println("N1 - listar máquinas disponíveis de alguma empresa");
                    System.out.println("N2 - listar usuários de alguma empresa");
                    System.out.println("N3 - Cadastrar usuário ");
                }

                if (usuario instanceof Adiministrador) {
                    System.out.println("Opções de Administrador");
                    System.out.println("A1 - listar máquinas disponíveis da sua instituição");
                    System.out.println("A2 - listar máquinas em uso da sua instituição");
                    System.out.println("A3 - listar usuários da sua instituição");
                    System.out.println("A4 - Cadastrar usuário ");
                }
                if (usuario instanceof Professor) {
                    System.out.println("Opções de Professor");
                    System.out.println("P1 - listar máquinas disponíveis da sua instituição");
                    System.out.println("P2 - listar máquinas em uso da sua instituição");
                    System.out.println("P3 - listar usuários da sua instituição");
                }

                System.out.println("----------|| Opções do sistema ||----------");
                System.out.println("1 - Ativar máquina");
                System.out.println("2 - Fechar sistema");
                System.out.println("-".repeat(15));

                opcaoUsuario = in.nextInt();

                switch (opcaoUsuario) {
                    case 1:
                        List<Maquina> maquinas = con.query("SELECT * FROM maquina WHERE emUso = 0",
                                new BeanPropertyRowMapper<>(Maquina.class));

                        System.out.println("-".repeat(15));
                        System.out.println("Escolha uma máquina disponível");

                        for (Maquina maquina : maquinas) {
                            System.out.println("id: " + maquina.getIdMaquina());
                            System.out.println("nome: " + maquina.getNome());
                            System.out.println("Sistema Operacional: " + maquina.getSO());
                            if (maquina.getDetalhes() != null) {
                                System.out.println("Detalhes: " + maquina.getDetalhes());
                            }
                        }

                        System.out.println("-".repeat(15));
                        System.out.println("Digite o número da máquina");
                        Integer numMaquina = in.nextInt();
                        ativarMaquina(con, numMaquina, histConsmRecurso);
                        numeroMaquina = numMaquina;
                        System.out.println("Digite o código da aula");
                        leitor.nextLine(); // Consumir a quebra de linha pendente
                        String codigoAula = leitor.nextLine();
                        histConsmRecurso.mostrarHistorico(numeroMaquina, codigoAula);
                        break;

                    case 2:
                        desativarMaquina(con, numeroMaquina);
                        exibirMensagemDespedida();
                        histConsmRecurso.fecharSistema();
                        return; // Isso encerrará o programa
                    default:
                        System.out.println("Opção inválida");
                }
            } while (opcaoUsuario != 2);
        } else {
            System.out.println("Dados de login inválidos");
        }
    }

    private static void ativarMaquina(JdbcTemplate con, Integer maquinaId, HistConsmRecurso histConsmRecurso) {
        Maquina maquina = con.queryForObject("SELECT * FROM maquina WHERE idMaquina = ? AND emUso = 0",
                new BeanPropertyRowMapper<>(Maquina.class), maquinaId);

        if (maquina != null) {
            con.update("UPDATE maquina SET emUso = 1 WHERE idMaquina = ?", maquinaId);
            System.out.println("Máquina ativada com sucesso: " + maquina.getNome());
        } else {
            System.out.println("Máquina não disponível ou inválida.");
        }
    }

    private static void desativarMaquina(JdbcTemplate con, Integer maquinaId) {
        con.update("UPDATE maquina SET emUso = 0 WHERE idMaquina = ?", maquinaId);
    }

    private static void visualizarEditarPerfil(JdbcTemplate con) {
        Scanner leitor = new Scanner(System.in);

        System.out.println("Digite o seu email:");
        String email = leitor.nextLine();
        while (!email.contains("@")) {
            System.out.println("O email deve conter o caractere '@'. Tente novamente:");
            email = leitor.nextLine();
        }

        System.out.println("Digite a sua senha (mínimo de 8 caracteres):");
        String senha = leitor.nextLine();
        while (senha.length() < 8) {
            System.out.println("A senha deve ter no mínimo 8 caracteres. Tente novamente:");
            senha = leitor.nextLine();
        }

        List<Usuario> usuarios = con.query("SELECT * FROM usuario WHERE email = ? AND senha = ?",
                new BeanPropertyRowMapper<>(Usuario.class), email, senha);

        if (usuarios.size() > 0) {
            Usuario usuario = usuarios.get(0);

            System.out.println("Perfil do usuário:");
            System.out.println("Nome: " + usuario.getNome());
            System.out.println("Email: " + usuario.getEmail());
            System.out.println("Senha: " + usuario.getSenha());
            System.out.println("Instituição: " + usuario.getFkInstituicao());

            System.out.println("Deseja editar o perfil? (1 - Sim, 2 - Não)");
            int editarPerfil = leitor.nextInt();

            if (editarPerfil == 1) {
                editarPerfilUsuario(con, usuario);
            }
        } else {
            System.out.println("Dados de login inválidos");
        }
    }

    private static void editarPerfilUsuario(JdbcTemplate con, Usuario usuario) {
        Scanner leitor = new Scanner(System.in);

        System.out.println("Digite o novo nome do usuário:");
        String novoNome = leitor.nextLine();
        if (!novoNome.isEmpty()) {
            usuario.setNome(novoNome);
        }

        System.out.println("Digite o novo email do usuário:");
        String novoEmail = leitor.nextLine();
        if (!novoEmail.isEmpty() && novoEmail.contains("@")) {
            usuario.setEmail(novoEmail);
        }

        System.out.println("Digite a nova senha do usuário (mínimo de 8 caracteres):");
        String novaSenha = leitor.nextLine();
        if (!novaSenha.isEmpty() && novaSenha.length() >= 8) {
            usuario.setSenha(novaSenha);
        }

        // Atualiza as informações no banco de dados
        con.update("UPDATE usuario SET nome = ?, email = ?, senha = ? WHERE idUsuario = ?",
                usuario.getNome(), usuario.getEmail(), usuario.getSenha(), usuario.getIdUsuario());

        System.out.println("Perfil editado com sucesso!");
    }

    private static void enviarSugestao(JdbcTemplate con, Scanner in) {
        Scanner leitor = new Scanner(System.in);

        System.out.println("Digite a sua sugestão:");
        String sugestao = leitor.nextLine();

        // Exibe a sugestão no console
        System.out.println("Sugestão enviada: " + sugestao);
        System.out.println("Sugestão enviada com sucesso!");
    }


    private static void exibirMensagemDespedida() {
        System.out.println("+---------------------------------------------------------------+");
        System.out.println("|                      \u001B[38;2;109;73;157mMAGISTER\u001B[0m                         |");
        System.out.println("|   \u001B[38;2;109;73;157mObrigado por usar o nosso sistema\u001B[0m               |"); // Cor personalizada
        System.out.println("|   \u001B[38;2;109;73;157mTenha um ótimo dia e uma vida incrível\u001B[0m      |"); // Cor personalizada
        System.out.println("|                                                                   |");
        System.out.println("|                        \u001B[38;2;109;73;157m#6D499D\u001B[0m                     |"); // Cor personalizada
        System.out.println("+---------------------------------------------------------------+");
    }

    private static void cadastrarMaquina(JdbcTemplate con) {
        Scanner leitor = new Scanner(System.in);

        System.out.println("Digite o nome da máquina:");
        String nomeMaquina = leitor.nextLine();

        System.out.println("Digite o sistema operacional da máquina:");
        String sistemaOperacional = leitor.nextLine();

        System.out.println("A máquina está em uso? (Digite 1 para sim, 0 para não):");
        int emUso = leitor.nextInt();

        System.out.println("Digite o ID da instituição (fkInstituicao):");
        int fkInstituicao = leitor.nextInt();

        // Cadastrar a máquina no banco de dados
        con.update("INSERT INTO maquina (nome, SO, emUso, fkInstituicao) VALUES (?, ?, ?, ?)",
                nomeMaquina, sistemaOperacional, emUso, fkInstituicao);

        // Recuperar o ID da máquina recém-cadastrada
        Integer idMaquina = con.queryForObject("SELECT MAX(idMaquina) FROM maquina", Integer.class);

        // Cadastrar hardware e componente para a máquina
        cadastrarHardwareEComponente(con, idMaquina);
        cadastrarHardwareEComponente(con, idMaquina);
        cadastrarHardwareEComponente(con, idMaquina);

        System.out.println("Máquina cadastrada com sucesso!");
    }

    private static void cadastrarHardwareEComponente(JdbcTemplate con, Integer idMaquina) {
        Scanner leitor = new Scanner(System.in);

        // Cadastrar hardware
        System.out.println("Digite o fabricante do hardware:");
        String fabricante = leitor.nextLine();

        System.out.println("Digite o modelo do hardware:");
        String modelo = leitor.nextLine();

        System.out.println("Digite a capacidade do hardware:");
        double capacidade = leitor.nextDouble();


        System.out.println("Digite a especificidade do hardware:");
        String especificidade = leitor.nextLine();

        // Cadastrar o tipo de hardware
        System.out.println("Digite o tipo de hardware (ex: CPU, GPU, Memória):");
        String tipoHardware = leitor.nextLine();

        // Cadastrar o tipo de hardware no banco de dados
        con.update("INSERT INTO tipoHardware (tipo) VALUES (?)", tipoHardware);

        // Recuperar o ID do tipo de hardware recém-cadastrado
        Integer fkTipoHardware = con.queryForObject("SELECT MAX(idTipoHardware) FROM tipoHardware", Integer.class);

        // Cadastrar o hardware no banco de dados, incluindo fkTipoHardware
        con.update("INSERT INTO hardware (fabricante, modelo, capacidade, especificidade, fkTipoHardware) VALUES (?, ?, ?, ?, ?)", fabricante, modelo, capacidade, especificidade, fkTipoHardware);

        // Recuperar o ID do hardware recém-cadastrado
        Integer idHardware = con.queryForObject("SELECT MAX(idHardware) FROM hardware", Integer.class);

        // Cadastrar componente
        System.out.println("Digite a porcentagem máxima para o componente (deixe em branco para usar o valor padrão):");
        String inputMax = leitor.nextLine();
        Integer max = (inputMax.isEmpty()) ? null : Integer.parseInt(inputMax);

        // Cadastrar o componente no banco de dados, incluindo fkTipoHardware
        con.update("INSERT INTO componente (max, fkMaquina, fkHardware) VALUES (?, ?, ?)", max, idMaquina, idHardware);

        System.out.println("Hardware e componente cadastrados com sucesso!");
    }


    private static Integer cadastrarTipoHardware(JdbcTemplate con) {
        Scanner leitor = new Scanner(System.in);

        System.out.println("Digite o nome do tipo de hardware (ex: CPU, GPU, Memória):");
        String tipoHardware = leitor.nextLine();

        // Cadastrar o tipo de hardware no banco de dados
        con.update("INSERT INTO tipoHardware (tipo) VALUES (?)", tipoHardware);

        // Recuperar o ID do tipo de hardware recém-cadastrado
        return con.queryForObject("SELECT MAX(idTipoHardware) FROM tipoHardware", Integer.class);
    }

}
