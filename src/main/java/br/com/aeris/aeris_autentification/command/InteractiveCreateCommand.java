package br.com.aeris.aeris_autentification.command;

import br.com.aeris.aeris_autentification.model.Empresa;
import br.com.aeris.aeris_autentification.model.Usuario;
import br.com.aeris.aeris_autentification.repository.EmpresaRepositoty;
import br.com.aeris.aeris_autentification.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class InteractiveCreateCommand implements CommandLineRunner{
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepositoty empresaRepositoty;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // mvn spring-boot:run -Dspring-boot.run.arguments='create-user-interactive'
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

            System.out.print("Id empresa: ");
            String id = scanner.nextLine();


            // Criar novo usuário
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setSobrenome(sobrenome);
            usuario.setEmail(email);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setTipo("adm");
            usuario.setAtivo(true);
            usuario.setEmpresa(empresaRepositoty.getReferenceById(Long.parseLong(id)));

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

        // mvn spring-boot:run -Dspring-boot.run.arguments='create-empresa-interactive'
        if (args.length > 0 && args[0].equals("create-empresa-interactive")) {

            Scanner scanner = new Scanner(System.in);

            System.out.println("\n=== CRIAR NOVA EMPRESA ===\n");

            System.out.print("Nome: ");
            String nome = scanner.nextLine();


            // Criar nova empresa
            Empresa empresa = new Empresa();
            empresa.setNome(nome);

            empresaRepositoty.save(empresa);

            System.out.println("\n✅ Empresa criado com sucesso!");
            System.out.println("═══════════════════════════");
            System.out.println("Nome: " + nome);
            System.out.println("ID: " + empresa.getId());
            System.out.println("═══════════════════════════\n");

            scanner.close();
            System.exit(0);
        }
    }
}
