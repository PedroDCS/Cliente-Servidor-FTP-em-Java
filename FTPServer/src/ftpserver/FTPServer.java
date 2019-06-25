package ftpserver;



//Bibliotecas necessarias para o funcionamento do programa
import java.net.*;
import java.io.*;

//Classe principal da aplicação do servidor FTP
public class FTPServer {

    public static void main(String args[]) throws Exception {
        ServerSocket soc = new ServerSocket(5217); // Define a porta do servidor
        System.out.println("FTP Server Started on Port Number 5217");
        while (true) {
            System.out.println("Waiting for Connection ...");
            transferfile t = new transferfile(soc.accept());

        }
    }
}

//classe responsavel pela transferencia de arquivos entre o cliente e o servidor
class transferfile extends Thread {

    Socket ClientSoc;

    DataInputStream din;
    DataOutputStream dout;

    transferfile(Socket soc) {
        try {
            ClientSoc = soc;
            din = new DataInputStream(ClientSoc.getInputStream());
            dout = new DataOutputStream(ClientSoc.getOutputStream());
            System.out.println("FTP Client Connected ...");
            start();

        } catch (Exception ex) {
        }
    }

    // Função para enviar arquivos ao cliente
    void SendFile() throws Exception {
        String filename = din.readUTF(); // Recebe o nome do arquivo a ser enviado ao cliente
        String caminho = "./Arquivos/";
        String arquivo = caminho.concat(filename);
        File f = new File(arquivo); // Tenta criar o arquivo
        if (!f.exists()) { // Se o arquivo não for encontrado
            dout.writeUTF("File Not Found");
            return;
        } else { // Se for encontrado
            dout.writeUTF("READY"); // Indica que esta pronto para enviar o arquivo
            FileInputStream fin = new FileInputStream(f);
            int ch;
            do {
                ch = fin.read();
                dout.writeUTF(String.valueOf(ch));
            } while (ch != -1);
            fin.close();
            dout.writeUTF("File Receive Successfully");
        }
    }

    // Recebe o arquivo de um cliente
    void ReceiveFile() throws Exception {
        String filename = din.readUTF(); // Recebe o nome do arquivo
        if (filename.compareTo("File not found") == 0) { 
            return;
        }
        
        String caminho = "./Arquivos/";
        String arquivo = caminho.concat(filename);
        File f = new File(arquivo); // Tenta criar o arquivo
        String option;

        if (f.exists()) { // Verifica se o arquivo ja existe, se existir chama um menu de opções
            dout.writeUTF("File Already Exists");
            option = din.readUTF();
        } else {
            dout.writeUTF("SendFile");
            option = "Y";
        }

        // Se o arquivo não existir, ou se ele sera sobrescrito
        if (option.compareTo("Y") == 0) {
            FileOutputStream fout = new FileOutputStream(f);
            int ch;
            String temp;
            do {
                temp = din.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1) {
                    fout.write(ch);
                }
            } while (ch != -1);
            fout.close();
            dout.writeUTF("File Send Successfully");
        } else { // Caso o usuario não deseje sobrescrever o arquivo 
            return;
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
    
    
    // Inicia o servidor
    public void run() {
        while (true) {
            try {
                System.out.println("Waiting for Command ...");
                
                String Command = din.readUTF(); // Recebe um comando do usuario
                //Aqui verifica qual opcao vai acessar no momento
                if (Command.compareTo("GET") == 0) { // Caso ele deseje baixar um arquivo do servidor
                    System.out.println("\tGET Command Received ...");
                    SendFile();
                    continue;
                } else if (Command.compareTo("SEND") == 0) { // Caso ele deseje enviar um arquivo para o servidor
                    System.out.println("\tSEND Command Receiced ...");
                    ReceiveFile();
                    continue;
                } else if (Command.compareTo("LIST") == 0) { // 
                    System.out.println("\tListando Arquivos do servidor");
                    ListaArquivos();
                    continue;
                }else if (Command.compareTo("DISCONNECT") == 0) { // Caso ele deseje fechar o servidor
                    System.out.println("\tDisconnect Command Received ...");
                    System.exit(1);
                } 
            } catch (Exception ex) {
            }
        }
    }
}
