package ftpcliente;


//Bibliotecas necessarias para o funcionamento do programa
import java.net.*;
import java.io.*;

//classe principal da aplicação do ciente FTP
public class FTPCliente {
    
    public static void main(String args[]) throws Exception {
        
        BufferedReader entrada_teclado;
        entrada_teclado = new BufferedReader(new InputStreamReader(System.in));
        String ip_server;//declaracao da string que ira guardar o endereço IP do servidor
        System.out.println("Insira o endereço IP do servidor");
        ip_server = entrada_teclado.readLine();//salva o endereço na variavel
        //127.0.0.1
        Socket soc = new Socket(ip_server, 5217);// Define o IP e a porta do servidor
        transferenciaArquivosCliente t = new transferenciaArquivosCliente(soc);
        t.menuPrincipal();
    }
}

//classe responsavel pela transferencia de arquivos entre o cliente e o servidor
class transferenciaArquivosCliente {

    //
    Socket ClienteSocket;

    DataInputStream dados_input;
    DataOutputStream dados_output;
    BufferedReader entrada_teclado;

    transferenciaArquivosCliente(Socket soc) {
        try {
            ClienteSocket = soc;
            dados_input = new DataInputStream(ClienteSocket.getInputStream());
            dados_output = new DataOutputStream(ClienteSocket.getOutputStream());
            entrada_teclado = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception ex) {
        }
    }

    //funcao para enviar arquivos ao servidor
    void EnviarArquivo() throws Exception {
        
        String nome_arquivo;//declaracao dda string que ira guardar o nome do arquivo
        System.out.print("Digite o nome do Arquivo:");//O usuario devera entrar com o nome do arquivo a ser enviado ao servidor
        nome_arquivo = entrada_teclado.readLine();//salva o nome do arquivo na variavel

        String caminho = "./Arquivos/";
        String arquivo = caminho.concat(nome_arquivo);
        File f = new File(arquivo); // Cria arquivo na memoria
        if (!f.exists())//verifica se o arquivo existe
        {
            System.out.println("Arquivo nao Existe...");
            dados_output.writeUTF("Arquivo nao Encontrado");
            return;
        }

        dados_output.writeUTF(nome_arquivo);

        String msg_servidor = dados_input.readUTF();

        if (msg_servidor.compareTo("Arquivo Ja Existe") == 0)//verifica se o arquivo ja existe no servidor
        {
            String opcao;
            System.out.println("Arquivo Ja Existe. Deseja Substitui-lo (Y/N)(S/N) ?");
            opcao = entrada_teclado.readLine();
            //verifica se o usuario deseja substituir o arquivo no servidor
            if (opcao.compareTo("Y") == 0 || opcao.compareTo("y") == 0 ||
                    opcao.compareTo("S") == 0 || opcao.compareTo("s") == 0) {
                dados_output.writeUTF("Y");
            } else {
                dados_output.writeUTF("N");
                return;
            }
        }

        //envia o arquivo ao servidor caso nao ocorra nenhum impedimento
        System.out.println("Enviando Arquivo...");
        FileInputStream fin = new FileInputStream(f);
        int ch;
        do {
            ch = fin.read();
            dados_output.writeUTF(String.valueOf(ch));
        } while (ch != -1);
        fin.close();
        System.out.println(dados_input.readUTF());

    }

    //funcao para receber arquivos do servidor
    void RecebeArquivo() throws Exception {
        String nomeArquivo;
        System.out.print("Digite o nome do Arquivo: ");
        nomeArquivo = entrada_teclado.readLine();
        dados_output.writeUTF(nomeArquivo);

        //verifica se o arquivo existe no servidor
        String msg_servidor = dados_input.readUTF();
        if (msg_servidor.compareTo("Arquivo Nao Encontrado") == 0) {
            System.out.println("Arquivo Nao Encontrado no Servidor...");
            return;
        } else if (msg_servidor.compareTo("PRONTO") == 0)//caso o arquivo exista, o cliente ira receber o arquivo
        {
            System.out.println("Recebendo Arquivo...");
            String caminho = "./Arquivos/";
            String arquivo = caminho.concat(nomeArquivo);
            File f = new File(arquivo); // Tenta criar o arquivo
            if (f.exists())//verifica se o arquivo ja existe no cliente, caso exista, pergunta se deseja substituir o arquivo existente
            {
                String opcao;
                System.out.println("Arquivo Ja Existe. Deseja Substitui-lo (Y/N)(S/N) ?");
                opcao = entrada_teclado.readLine();
                if (opcao.compareTo("N") == 0 || opcao.compareTo("n") == 0) {
                    dados_output.flush();
                    return;
                }
            }
            //recebe o arquivo
            FileOutputStream fout = new FileOutputStream(f);
            int ch;
            String temp;
            do {
                temp = dados_input.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1) {
                    fout.write(ch);
                }
            } while (ch != -1);
            fout.close();
            System.out.println(dados_input.readUTF());

        }

    }

    
    
    
void ListaArquivos() throws Exception {
    File raiz = new File("./Arquivos/");
    for(File f: raiz.listFiles()) {
        if(f.isFile()) {
            System.out.println(f.getName());
        }
    }
}
     
    //menu com opcoes 
    public void menuPrincipal() throws Exception {
        
        
        Integer log = 0; 
        while (log ==0) {
            String Usuario, Senha;
            String login = "";
            String aux = "";
                    
                    
            System.out.println("Login no servidor");
            System.out.print("Usuario: ");
            Usuario = entrada_teclado.readLine();
            
            System.out.print("Senha: ");
            Senha = entrada_teclado.readLine();
            
            
            dados_output.writeUTF("LOGIN");
            
            aux = dados_input.readUTF(); // Recebe um comando do usuario
                
            if (aux.compareTo("ESPERANDO") == 0) { //
                
                
                login = login.concat(Usuario);
                login = login.concat("-");
                login = login.concat(Senha);
                dados_output.writeUTF(login);
                
                
            }else{
                System.out.println("\n\n\nErro, Tente Novamente\n\n");
                continue;
            }
            aux = dados_input.readUTF();
            if (aux.compareTo("SUCESSO") == 0) { //
                log =1;
                
            }else{
                System.out.println("\nDados Incorretos, Tente Novamente");
                log=0;
            }
        }
  
        while (true) {
            System.out.println("[ MENU ]");
            System.out.println("1. Enviar Arquivo");
            System.out.println("2. Receber Arquivo");
            System.out.println("3. Listar arquivos do Servidor");
            System.out.println("4. Listar arquivos do Cliente");
            System.out.println("5. Sair");
            System.out.print("\nDigite uma Opcao :");
            int choice;
            choice = Integer.parseInt(entrada_teclado.readLine());
            if (choice == 1) {
                dados_output.writeUTF("ENVIAR");
                EnviarArquivo();
                System.out.println("\nPressione qualquer tecla para continuar");
                String temp = entrada_teclado.readLine();
            } else if (choice == 2) {
                dados_output.writeUTF("RECEBER");
                RecebeArquivo();
                System.out.println("\nPressione qualquer tecla para continuar");
                String temp = entrada_teclado.readLine();
            }else if (choice == 3) {
                dados_output.writeUTF("LISTA");
                String temp;
                temp = dados_input.readUTF();
                System.out.println(temp);
                System.out.println("\nPressione qualquer tecla para continuar");
                temp = entrada_teclado.readLine();
                //ListaArquivos();
            }else if (choice == 4) {
                ListaArquivos();
                System.out.println("\nPressione qualquer tecla para continuar");
                String temp = entrada_teclado.readLine();
            } else {
                dados_output.writeUTF("DESCONECTAR");
                System.exit(1);
            }
        }
    }
}
