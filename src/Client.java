import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class Client{
    private JFrame frame1;
    private JTextArea contentArea;
    private JTextField txt_message;
    private JTextField txt_ip;
    private JTextField txt_port;
    private JButton btn_send;
    private JButton btn_connect;
    private JButton btn_discon;
    private JPanel northPanel;
    private JPanel southPanel;
    private JScrollPane rightPanel;
    private DefaultListModel listModel;
    Socket clientSocket;
    ClientThread clientThread;
    DataOutputStream dout;
    /*主函数*/
    public static void main(String args[]) throws Exception {
        new Client();
    }
    public Client(){
        frame1 = new JFrame("客户端");
        txt_ip = new JTextField("127.0.0.1");
        txt_port = new JTextField("8089");
        // 更改JFrame的图标：
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setForeground(Color.blue);
        txt_message = new JTextField();
        btn_send = new JButton("发送");
        btn_connect = new JButton("连接");
        btn_discon = new JButton("断开");
        btn_discon.setEnabled(false);
        listModel = new DefaultListModel();

        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 6));
        northPanel.add(txt_ip);
        northPanel.add(new JLabel("IP"));
        northPanel.add(txt_port);
        northPanel.add(new JLabel("端口"));
        northPanel.add(btn_connect);
        northPanel.add(btn_discon);
        northPanel.setBorder(new TitledBorder("服务器配置信息"));


        southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new TitledBorder("消息栏"));
        southPanel.add(northPanel, "North");
        southPanel.add(txt_message, "Center");
        southPanel.add(btn_send, "East");

        rightPanel = new JScrollPane(contentArea);
        rightPanel.setBorder(new TitledBorder("聊天消息"));



        frame1.setLayout(new BorderLayout());
        frame1.add(rightPanel, "Center");
        frame1.add(southPanel, "South");
        frame1.setSize(600, 400);
        //frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());//设置全屏
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame1.setLocation((screen_width - frame1.getWidth()) / 2,
                (screen_height - frame1.getHeight()) / 2);
        frame1.setVisible(true);




//          当点击连接是建立socket连接
        btn_connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ip = txt_ip.getText().trim();
                String tmp_port = txt_port.getText().trim();
                if(ip==null||ip.equals("")||tmp_port==null||tmp_port.equals("")){
                    contentArea.append("请输入完成的ip和端口号\r\n");
                    return;
                }
                int port = Integer.parseInt(tmp_port);
                connect(ip,port);
            }
        });

        // 单击断开连接按钮时事件
        btn_discon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeConnection();
            }
        });
        // 写消息的文本框中按回车键时事件
        txt_message.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                send();
            }
        });

        // 单击发送按钮时事件
        btn_send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });
    }
    public void connect(String ip,int port){
        try{
            clientSocket=new Socket(ip,port);
            dout = new DataOutputStream( clientSocket.getOutputStream() );
            contentArea.append("连接服务器成功！本地信息为"+clientSocket.getLocalAddress()+":"+clientSocket.getLocalPort()+"\r\n");
            txt_port.setEnabled(false);
            txt_ip.setEnabled(false);
            btn_connect.setEnabled(false);
            btn_discon.setEnabled(true);
        }catch (Exception ex){
            contentArea.append("连接服务器出错："+ex.getMessage()+"\r\n");
        }
        try{
            DataInputStream din = new DataInputStream(clientSocket.getInputStream());
            // 为接收数据开启后台线程
            clientThread = new ClientThread(clientSocket,din);
//            new clienttransfer(clientSocket,dout);
        } catch( IOException ie ) { System.out.println( ie ); }
    }
    public void send() {
        String message = txt_message.getText().trim();
        if (message == null || message.equals("")) {
            JOptionPane.showMessageDialog(frame1, "消息不能为空！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 获取下一条信息
        try{
            dout.writeUTF(clientSocket.getLocalAddress()+":"+clientSocket.getLocalPort()+" says:"+message);
        }catch (Exception ex){
            System.out.println("输入信息传输出错："+ex.getMessage());
        }
        // 打印在文本窗口中
        System.out.println(message);
        txt_message.setText(null);
    }
    public void send(String message){
        try{
            dout.writeUTF(message);
            if(message.equals("CLOSE")){
                contentArea.append("断开连接成功！\r\n");
                txt_port.setEnabled(true);
                txt_ip.setEnabled(true);
                btn_connect.setEnabled(true);
                btn_discon.setEnabled(false);
            }
        } catch (Exception ex){
            contentArea.append(message+" 指令发送错误："+ex.getMessage()+"\r\n");
        }
    }

    class ClientThread extends Thread {
        private Socket socket;
        private DataInputStream din;
        public ClientThread(Socket socket,DataInputStream din){
            this.socket=socket;
            this.din=din;
            frame1.setTitle("客户端  "+clientSocket.getInetAddress()+":"+clientSocket.getLocalPort());
            start();
        }
        public void run() {
            String message;
            try {
                // 接受信息一个到一个，一直循环
                while (true) {
                    // 获取下一条信息
                    message = din.readUTF();
                    // 打印在文本窗口中
                    contentArea.append(message+"\r\n");
                    //______________ta.append( message+"\n" );
                }
            } catch( IOException ie ) { System.out.println( ie ); }
        }
    }

    @SuppressWarnings("deprecation")
    public synchronized boolean closeConnection() {
        try {
            send("CLOSE");// 发送断开连接命令给服务器
            clientThread.stop();// 停止接受消息线程
            // 释放资源
            if (clientSocket != null) {
                clientSocket.close();
            }
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
            return false;
        }
    }
}
