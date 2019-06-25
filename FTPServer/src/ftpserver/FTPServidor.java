package ftpserver;



//Bibliotecas necessarias para o funcionamento do programa
import java.net.*;
import java.util.Calendar;
import java.io.*;

//Classe principal da aplicação do servidor FTP
public class FTPServidor {

    public static void main(String args[]) throws Exception {
        FileWriter Log = new FileWriter("Log.txt", true);
        PrintWriter escreverLog = new PrintWriter(Log);
        Calendar data;
        int Porta = 5217;	    	

        ServerSocket soc = new ServerSocket(Porta); // Define a porta do servidor
        System.out.printf("Servidor FTP Iniciado na Porta %d\n", Porta);

        data = Calendar.getInstance();
        escreverLog.printf(data.getTime() + " Servidor aberto na porta: %d", Porta);
        escreverLog.println();
        Log.close();
        
        while (true) {
            System.out.println("Aguardando Conexao...");
            transferencia_arquivos t = new transferencia_arquivos(soc.accept());

        }
    }
}

//classe responsavel pela transferencia de arquivos entre o cliente e o servidor
class transferencia_arquivos extends Thread {

    Socket ClientSoc;

    DataInputStream dados_input;
    DataOutputStream dados_output;
    
    Calendar data;

    transferencia_arquivos(Socket soc) {
        try {
        	
            FileWriter Log = new FileWriter("Log.txt", true);
            PrintWriter escreverLog = new PrintWriter(Log);

            data = Calendar.getInstance();
            escreverLog.printf(data.getTime() + " Cliente conectado");
            escreverLog.println();
            Log.close();        	
        	
            ClientSoc = soc;
            dados_input = new DataInputStream(ClientSoc.getInputStream());
            dados_output = new DataOutputStream(ClientSoc.getOutputStream());
            System.out.println("Cliente FTP Connectado...");
            start();

        } catch (Exception ex) {
        }
    }
    
    //Funcao para listar arquivos no servidor
    void ListaArquivos() throws Exception {
        File raiz = new File("./Arquivos/");

        String lista2 = "";
	for(File f: raiz.listFiles()) {
            if(f.isFile()) {
                //System.out.println(f.getName());
                lista2 = lista2.concat(f.getName());
                lista2 = lista2.concat("\n");
            }
	}
        dados_output.writeUTF(lista2);  
}
    
    // Função para enviar arquivos ao cliente
    void EnviarArquivo() throws Exception {
        
        FileWriter Log = new FileWriter("Log.txt", true);
	PrintWriter escreverLog = new PrintWriter(Log); 
        
        String nome_arquivo = dados_input.readUTF(); // Recebe o nome do arquivo a ser enviado ao cliente
        String caminho = "./Arquivos/";
        String arquivo = caminho.concat(nome_arquivo);
        File f = new File(arquivo); // Tenta criar o arquivo
        if (!f.exists()) { // Se o arquivo não for encontrado
            
            data = Calendar.getInstance();
            escreverLog.printf(data.getTime() + " Arquivo não encontrado no servidor.");
            escreverLog.println();
            Log.close();
            
            dados_output.writeUTF("Arquivo Nao Encontrado");
            return;
        } else { // Se for encontrado
            
            data = Calendar.getInstance();
            escreverLog.printf(data.getTime() + " Pronto para receber o arquivo.");
            escreverLog.println();
            
            dados_output.writeUTF("PRONTO"); // Indica que esta pronto para enviar o arquivo
            FileInputStream fin = new FileInputStream(f);
            int ch;
            do {
                ch = fin.read();
                dados_output.writeUTF(String.valueOf(ch));
            } while (ch != -1);
            fin.close();
            
            data = Calendar.getInstance();
            escreverLog.printf(data.getTime() + " Arquivo " + nome_arquivo + " recebido.");
            escreverLog.println();
            Log.close();
            
            dados_output.writeUTF("Arquivo Recebido Com Sucesso");
        }
    }

    // Recebe o arquivo de um cliente
    void ReceiveFile() throws Exception {
        
        FileWriter Log = new FileWriter("Log.txt", true);
	PrintWriter escreverLog = new PrintWriter(Log);
        
        String nome_arquivo = dados_input.readUTF(); // Recebe o nome do arquivo
        if (nome_arquivo.compareTo("Arquivo nao Encontrado") == 0) { 
            
            data = Calendar.getInstance();
            escreverLog.printf(data.getTime() + " Arquivo não encontrado no cliente.");
            escreverLog.println();
            Log.close();
            
            return;
        }
        
        String caminho = "./Arquivos/";
        String arquivo = caminho.concat(nome_arquivo);
        File f = new File(arquivo); // Tenta criar o arquivo
        String opcao;

        if (f.exists()) { // Verifica se o arquivo ja existe, se existir chama um menu de opções
            dados_output.writeUTF("Arquivo Ja Existe");
            
            data = Calendar.getInstance();
            escreverLog.printf(data.getTime() + " Arquivo " + nome_arquivo + " ja existe no servidor.");
            escreverLog.println();
            
            opcao = dados_input.readUTF();
        } else {
            dados_output.writeUTF("EnviandoArquivo");
            opcao = "Y";
        }

        // Se o arquivo não existir, ou se ele sera sobrescrito
        if (opcao.compareTo("Y") == 0 || opcao.compareTo("y") == 0) {
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
            dados_output.writeUTF("Arquivo Enviado Com Sucesso...");
            
            data = Calendar.getInstance();
            escreverLog.printf(data.getTime() + " Arquivo " + nome_arquivo + " enviado com sucesso.");
            escreverLog.println();
            Log.close();
            
        } else { // Caso o usuario não deseje sobrescrever o arquivo 
            Log.close();
            return;
        }

    }


    
    void Login()throws Exception {
        String aux = null;
        int aux1 = 0;
        dados_output.writeUTF("ESPERANDO");
        
        
        //Ate aqui veio
        aux = dados_input.readUTF();
        
        File Usuarios = new File("./Usuarios/");
	for(File f: Usuarios.listFiles()) {
            if(f.isFile()) {
                String usuariosenha = f.getName();
                if(aux.compareTo(usuariosenha) == 0){
                    aux1= 1;
                    
                    break;
                }else{
                    aux1=0;
                }
            }
	}
        if(aux1 == 1){
            dados_output.writeUTF("SUCESSO");
        }else{
        dados_output.writeUTF("FRACASSO");
    }
        return;
    }
    // Inicia o servidor
    public void run() {
        while (true) {
            try {
                
                FileWriter Log = new FileWriter("Log.txt", true);
                PrintWriter escreverLog = new PrintWriter(Log);
                data = Calendar.getInstance();
                escreverLog.printf(data.getTime() + " Esperando por comando.");
                escreverLog.println();
                
                System.out.println("Esperando Por Comando...");
                
                String Command = dados_input.readUTF(); // Recebe um comando do usuario
                //Aqui verifica qual opcao vai acessar no momento
                if (Command.compareTo("RECEBER") == 0) { // Caso ele deseje baixar um arquivo do servidor
                    System.out.println("\tComando 'RECEBER' Recebido...");
                    EnviarArquivo();
                    
                    data = Calendar.getInstance();
                    escreverLog.printf(data.getTime() + " Comando de baixar um arquivo do servidor recebido.");
                    escreverLog.println();
                    
                    continue;
                } else if (Command.compareTo("ENVIAR") == 0) { // Caso ele deseje enviar um arquivo para o servidor
                    System.out.println("\tComando 'ENVIAR' Recebido...");
                    ReceiveFile();
                    
                    data = Calendar.getInstance();
                    escreverLog.printf(data.getTime() + " Comando de enviar um arquivo para o servidor recebido.");
                    escreverLog.println();
                    
                    continue;
                } else if (Command.compareTo("LISTA") == 0) { // 
                    System.out.println("\tListando Arquivos do servidor");
                    ListaArquivos();
                    
                    data = Calendar.getInstance();
                    escreverLog.printf(data.getTime() + " Comando de listar arquivos do servidor recebido.");
                    escreverLog.println();
                    
                    continue;
                }else if (Command.compareTo("LOGIN") == 0) { // 
                    System.out.println("\tLogin no Servidor");
                    Login();
                    continue;
                }else if (Command.compareTo("DESCONECTAR") == 0) { // Caso ele deseje fechar o servidor
                    System.out.println("\tComando 'DESCONECTAR' Recebido......");
                    
                    data = Calendar.getInstance();
                    escreverLog.printf(data.getTime() + " Cliente desconectou do Servidor.");
                    escreverLog.println();
                    Log.close();
                    
                } 
            } catch (Exception ex) {
            }
        }
    }
}