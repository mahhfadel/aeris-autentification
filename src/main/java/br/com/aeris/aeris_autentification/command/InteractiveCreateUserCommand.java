package br.com.aeris.aeris_autentification.command;

import br.com.aeris.aeris_autentification.model.Usuario;
import br.com.aeris.aeris_autentification.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class InteractiveCreateUserCommand implements CommandLineRunner{
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verificar se foi passado o comando interativo
        if (args.length > 0 && args[0].equals("create-user-interactive")) {

            Scanner scanner = new Scanner(System.in);

            System.out.println("\n=== CRIAR NOVO USUÁRIO ===\n");

            System.out.print("Nome: ");
            String nome = scanner.nextLine();
            System.out.print("Sobrenome: ");
            String sobrenome = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            // Verificar se o email já existe
            if (usuarioRepository.existsByEmail(email)) {
                System.err.println("\nErro: Já existe um usuário com o email " + email);
                System.exit(1);
            }

            System.out.print("Senha: ");
            String senha = scanner.nextLine();


            // Criar novo usuário
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setSobrenome(sobrenome);
            usuario.setEmail(email);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setTipo("adm");
            usuario.setAtivo(true);

            usuarioRepository.save(usuario);

            System.out.println("\n✅ Usuário criado com sucesso!");
            System.out.println("═══════════════════════════");
            System.out.println("Nome: " + nome);
            System.out.println("Sobrenome: " + sobrenome);
            System.out.println("Email: " + email);
            System.out.println("═══════════════════════════\n");

            scanner.close();
            System.exit(0);
        }
    }
}
