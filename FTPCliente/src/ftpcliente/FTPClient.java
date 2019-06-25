package ftpcliente;


//Bibliotecas necessarias para o funcionamento do programa
import java.net.*;
import java.io.*;

//classe principal da aplicação do ciente FTP
public class FTPClient {
    
    public static void main(String args[]) throws Exception {
        
        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(System.in));
        String ip_server;//declaracao da string que ira guardar o endereço IP do servidor
        System.out.println("Insira o endereço IP do servidor");
        ip_server = br.readLine();//salva o endereço na variavel
        
        Socket soc = new Socket("127.0.0.1", 5217);// Define o IP e a porta do servidor
        transferfileClient t = new transferfileClient(soc);
        t.displayMenu();
    }
}

//classe responsavel pela transferencia de arquivos entre o cliente e o servidor
class transferfileClient {

    //
    Socket ClientSoc;

    DataInputStream din;
    DataOutputStream dout;
    BufferedReader br;

    transferfileClient(Socket soc) {
        try {
            ClientSoc = soc;
            din = new DataInputStream(ClientSoc.getInputStream());
            dout = new DataOutputStream(ClientSoc.getOutputStream());
            br = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception ex) {
        }
    }

    //funcao para enviar arquivos ao servidor
    void SendFile() throws Exception {

        String filename;//declaracao dda string que ira guardar o nome do arquivo
        System.out.print("Enter File Name :");//O usuario devera entrar com o nome do arquivo a ser enviado ao servidor
        filename = br.readLine();//salva o nome do arquivo na variavel

        String caminho = "./Arquivos/";
        String arquivo = caminho.concat(filename);
        File f = new File(arquivo); // Cria arquivo na memoria
        if (!f.exists())//verifica se o arquivo existe
        {
            System.out.println("File not Exists...");
            dout.writeUTF("File not found");
            return;
        }

        dout.writeUTF(filename);

        String msgFromServer = din.readUTF();

        if (msgFromServer.compareTo("File Already Exists") == 0)//verifica se o arquivo ja existe no servidor
        {
            String Option;
            System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
            Option = br.readLine();
            //verifica se o usuario deseja substituir o arquivo no servidor
            if (Option == "Y") {
                dout.writeUTF("Y");
            } else {
                dout.writeUTF("N");
                return;
            }
        }

        //envia o arquivo ao servidor caso nao ocorra nenhum impedimento
        System.out.println("Sending File ...");
        FileInputStream fin = new FileInputStream(f);
        int ch;
        do {
            ch = fin.read();
            dout.writeUTF(String.valueOf(ch));
        } while (ch != -1);
        fin.close();
        System.out.println(din.readUTF());

    }

    //funcao para receber arquivos do servidor
    void ReceiveFile() throws Exception {
        String fileName;
        System.out.print("Enter File Name :");
        fileName = br.readLine();
        dout.writeUTF(fileName);

        //verifica se o arquivo existe no servidor
        String msgFromServer = din.readUTF();
        if (msgFromServer.compareTo("File Not Found") == 0) {
            System.out.println("File not found on Server ...");
            return;
        } else if (msgFromServer.compareTo("READY") == 0)//caso o arquivo exista, o cliente ira receber o arquivo
        {
            System.out.println("Receiving File ...");
            String caminho = "./Arquivos/";
            String arquivo = caminho.concat(fileName);
            File f = new File(arquivo); // Tenta criar o arquivo
            if (f.exists())//verifica se o arquivo ja existe no cliente, caso exista, pergunta se deseja substituir o arquivo existente
            {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option = br.readLine();
                if (Option == "N") {
                    dout.flush();
                    return;
                }
            }
            //recebe o arquivo
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
            System.out.println(din.readUTF());

        }

    }

    
    
    
    void ListaArquivos() throws Exception {
        File raiz = new File("./Arquivos/");
	for(File f: raiz.listFiles()) {
            if(f.isFile()) {
                System.out.println(f.getName());
            }
	}
        
        
        
        String fileName;
        System.out.print("Enter File Name :");
        fileName = br.readLine();
        dout.writeUTF(fileName);

        //verifica se o arquivo existe no servidor
        String msgFromServer = din.readUTF();
        if (msgFromServer.compareTo("File Not Found") == 0) {
            System.out.println("File not found on Server ...");
            return;
        } else if (msgFromServer.compareTo("READY") == 0)//caso o arquivo exista, o cliente ira receber o arquivo
        {
            System.out.println("Receiving File ...");
            String caminho = "./Arquivos/";
            String arquivo = caminho.concat(fileName);
            File f = new File(arquivo); // Tenta criar o arquivo
            if (f.exists())//verifica se o arquivo ja existe no cliente, caso exista, pergunta se deseja substituir o arquivo existente
            {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option = br.readLine();
                if (Option == "N") {
                    dout.flush();
                    return;
                }
            }
            //recebe o arquivo
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
            System.out.println(din.readUTF());

        }
        
        
        
        
}
     
    //menu com opcoes 
    public void displayMenu() throws Exception {
        while (true) {
            System.out.println("[ MENU ]");
            System.out.println("1. Send File");
            System.out.println("2. Receive File");
            System.out.println("4. Listar arquivos");
            System.out.println("3. Exit");
            System.out.print("\nEnter Choice :");
            int choice;
            choice = Integer.parseInt(br.readLine());
            if (choice == 1) {
                dout.writeUTF("SEND");
                SendFile();
            } else if (choice == 2) {
                dout.writeUTF("GET");
                ReceiveFile();
            }
             else if (choice == 4) {
                dout.writeUTF("LIST");
                
                //ListaArquivos();
            } else {
                dout.writeUTF("DISCONNECT");
                System.exit(1);
            }
        }
    }
}
