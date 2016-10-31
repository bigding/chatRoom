import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class Client{
    private JFrame frame1;
    private JTextArea contentArea;
    private JTextField txt_message;
    private JButton btn_send;
    private JPanel southPanel;
    private JScrollPane rightPanel;
    private DefaultListModel listModel;
    Socket clientSocket = new Socket();
    /*主函数*/
    public static void main(String args[]) throws Exception {
        new Client();
    }
    public Client(){
        frame1 = new JFrame("客户端");
        // 更改JFrame的图标：
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setForeground(Color.blue);
        txt_message = new JTextField();
        btn_send = new JButton("发送");
        listModel = new DefaultListModel();

        southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new TitledBorder("消息栏"));
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
        try{
            clientSocket=new Socket("127.0.0.1",8089);
        }catch (Exception ex){
            System.out.println("链接服务器出错:"+ex.getMessage());
        }
        try{
            DataInputStream din = new DataInputStream( clientSocket.getInputStream() );
            DataOutputStream dout = new DataOutputStream( clientSocket.getOutputStream() );
            // 为接收数据开启后台线程
            new clientThread(clientSocket,din);
//            new clienttransfer(clientSocket,dout);
        } catch( IOException ie ) { System.out.println( ie ); }
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
    public void send() {
        String message = txt_message.getText().trim();
        if (message == null || message.equals("")) {
            JOptionPane.showMessageDialog(frame1, "消息不能为空！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 获取下一条信息
        try{
            DataOutputStream dout = new DataOutputStream( clientSocket.getOutputStream() );
            dout.writeUTF(clientSocket.getLocalAddress()+":"+clientSocket.getLocalPort()+" says:"+message);
        }catch (Exception ex){
            System.out.println("输入信息传输出错："+ex.getMessage());
        }
        // 打印在文本窗口中
        System.out.println(message);
        txt_message.setText(null);
    }

    class clientThread extends Thread {
        private Socket socket;
        private DataInputStream din;
        public clientThread(Socket socket,DataInputStream din){
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
    /*一下为当在控制台输入信息时的逻辑*/
//    class clienttransfer extends Thread{
//        private Socket socket;
//        private DataOutputStream dout;
//        public clienttransfer(Socket socket,DataOutputStream dout){
//            this.socket=socket;
//            this.dout=dout;
//            start();
//        }
//        public void run() {
//            String message;
//            try {
//                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//                // 接受信息一个到一个，一直循环
//                while (true) {
//                    message=br.readLine();
//                    if (message.equals("bye")){
//                        System.out.println("客户端下线,程序退出");
//                        System.exit(0);
//                    }
//                    dout.writeUTF(message);
//                    // 打印在文本窗口中
////                    System.out.println(message);
//
//                }
//            } catch( IOException ie ) { System.out.println( ie ); }
//        }
//    }
}
