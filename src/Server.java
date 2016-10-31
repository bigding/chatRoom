import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class Server{
//    private JFrame frame1;
//    private JTextArea contentArea;
//    private JTextField txt_message;
//    private JButton btn_send;
//    private JPanel southPanel;
//    private JScrollPane rightPanel;
//    private JScrollPane leftPanel;
//    private JSplitPane centerSplit;
//    private JList userList;
//    private DefaultListModel listModel;
    private static List list=new ArrayList();
    private ServerSocket wSocket;

    /*主函数*/
    public static void main(String args[]) throws Exception {
        new Server();
    }
    public Server(){
//        frame1 = new JFrame("服务器");
//        // 更改JFrame的图标：
//        contentArea = new JTextArea();
//        contentArea.setEditable(false);
//        contentArea.setForeground(Color.blue);
//        txt_message = new JTextField();
//        btn_send = new JButton("发送");
//        listModel = new DefaultListModel();
//        userList = new JList(listModel);
//
//        southPanel = new JPanel(new BorderLayout());
//        southPanel.setBorder(new TitledBorder("消息栏"));
//        southPanel.add(txt_message, "Center");
//        southPanel.add(btn_send, "East");
//
//        rightPanel = new JScrollPane(contentArea);
//        rightPanel.setBorder(new TitledBorder("聊天消息"));
//
//
//
//        frame1.setLayout(new BorderLayout());
//        frame1.add(rightPanel, "Center");
//        frame1.add(southPanel, "South");
//        frame1.setSize(600, 400);
//        frame1.setTitle("服务器端");
//        //frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());//设置全屏
//        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
//        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
//        frame1.setLocation((screen_width - frame1.getWidth()) / 2,
//                (screen_height - frame1.getHeight()) / 2);
//        frame1.setVisible(true);

        new ServerThread();
    }


    class ServerThread extends Thread {

        int clientCount=0;
        public ServerThread(){
            try{
                wSocket = new ServerSocket(8089);
            }catch (Exception ex){
                System.out.println("建立socket发生错误："+ex.getMessage());
            }
            start();
        }

        public void run(){
            Socket cSocket = new Socket();
            while(true)
            {
                //客户端计数
                clientCount++;
                // 抓取下一个来的连接
                try{
                    cSocket=wSocket.accept();
                }catch (Exception ex){
                    System.out.println("开启母线程出错");
                }
                list.add(cSocket);
                // 告诉大家我们得到它了
                System.out.println( "新的连接已建立 "+cSocket );
                System.out.println("当前在线的用户数: " + clientCount);
                // 为了写出数据给其他方面，创建一个DataOutputStream
                try {
                    DataOutputStream dout = new DataOutputStream( cSocket.getOutputStream() );
                }catch (Exception ex){
                    System.out.println("获取输入流出错");
                }
                // 为该连接创建一个新线程，之后忘记它
                ForAClientThread forAClientThread = new ForAClientThread(cSocket,list);
            }
        }
    }
    class ForAClientThread extends Thread{
        private Socket socket;
        private List list;
        // 构造函数
        public ForAClientThread(Socket socket,List list) {
            // 保存参数
            this.socket = socket;
            this.list=list;
            // 启动线程
            start();
        }
        public void run() {
            try {
                // 为通信创建DataInputStream;客户端使用DataOutputStream输出给我们
                DataInputStream din = new DataInputStream(socket.getInputStream());
                // 一直循环
                while (true) {
                    // ... 读取下一条信息 ...
                    String message = din.readUTF();

                    // ... 服务端发送它给所有的客户端
                    for(Object o:list){
                        Socket sc=(Socket)o;
                        DataOutputStream dout=new DataOutputStream(sc.getOutputStream());
                        dout.writeUTF(message + "(发向全员)");
                        System.out.println(message + "(发向全员)");
                    }
                }
            } catch (EOFException ie) {
                // 不需要错误信息
            } catch (IOException ie) {
                // 需要错误信息，输出至控制台
                ie.printStackTrace();
            } finally {
                System.out.println("服务器对单个客户端的连接出错");
            }
        }
    }
}
