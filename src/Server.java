import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class Server{
    private JFrame frame1;
    private JTextArea contentArea;
    private JTextField txt_port;
    private JTextField txt_message;
    private JButton btn_send;
    private JButton btn_start;
    private JButton btn_stop;
    private JPanel northPanel;
    private JPanel southPanel;
    private JScrollPane rightPanel;
    private JScrollPane leftPanel;
    private JSplitPane centerSplit;
    private DefaultListModel listModel;
    private List list=new ArrayList();
    private ServerSocket wSocket;

    /*主函数*/
    public static void main(String args[]) throws Exception {
        new Server();
    }
    public Server(){
        frame1 = new JFrame("服务器");
        txt_port = new JTextField("8089");
        btn_start = new JButton("启动");
        btn_stop = new JButton("停止");
        btn_stop.setEnabled(false);
        // 更改JFrame的图标：
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setForeground(Color.blue);
        txt_message = new JTextField();
        btn_send = new JButton("发送");
        listModel = new DefaultListModel();

        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 6));
        northPanel.add(txt_port);
        northPanel.add(new JLabel("端口"));
        northPanel.add(btn_start);
        northPanel.add(btn_stop);
        northPanel.setBorder(new TitledBorder("配置信息"));

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
        frame1.setTitle("服务器端");
        //frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());//设置全屏
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame1.setLocation((screen_width - frame1.getWidth()) / 2,
                (screen_height - frame1.getHeight()) / 2);
        frame1.setVisible(true);

//        new ServerThread();   /*快捷开启进程*/

//        当点击发送按钮时，将所有的信息发送给每个客户端
        btn_send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = txt_message.getText().trim();
                message = "server says:"+message;
                send(list,message);
            }
        });
//        当单击开启时，开启服务器
        btn_start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String tmp_port = txt_port.getText().trim();
                if(tmp_port == null || tmp_port.equals("")){
                    contentArea.append("请输入正确的端口号，再启动服务器\r\n");
                    return;
                }
                int port = Integer.parseInt(tmp_port);
                new ServerThread(port);
                btn_start.setEnabled(false);
                btn_stop.setEnabled(true);
                txt_port.setEnabled(false);
            }
        });
    }

    class ServerThread extends Thread {

        int clientCount=0;
        public ServerThread(int port){
            try{
                wSocket = new ServerSocket(port);
                contentArea.append("服务器启动成功！监听端口："+port+"\r\n");
            }catch (Exception ex){
                contentArea.append("服务器启动发生错误："+ex.getMessage()+"发向全员");
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
                    contentArea.append("开启母线程出错:"+ex.getMessage()+"\r\n");
                }
                list.add(cSocket);
                // 告诉大家我们得到它了
                contentArea.append( cSocket+"上线 当前在线的用户数: " + clientCount+"\r\n");
                // 为了写出数据给其他方面，创建一个DataOutputStream
                try {
                    DataOutputStream dout = new DataOutputStream( cSocket.getOutputStream() );
                }catch (Exception ex){
                    contentArea.append("获取输入流出错");
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
                    send(list,message);
//                    for(Object o:list){
//                        Socket sc=(Socket)o;
//                        DataOutputStream dout=new DataOutputStream(sc.getOutputStream());
//                        dout.writeUTF(message + "(发向全员)");
//                        System.out.println(message + "(发向全员)");
//                    }
                }
            } catch (EOFException ie) {
                // 不需要错误信息
            } catch (IOException ie) {
                // 需要错误信息，输出至控制台
                ie.printStackTrace();
            } finally {
                contentArea.append("服务器对单个客户端的连接出错");
            }
        }

    }
    //    发送信息的函数
    void send(List list,String message){
        contentArea.append(message+"\r\n");
        for(Object o:list){
            Socket sc=(Socket)o;
            try {
                DataOutputStream dout=new DataOutputStream(sc.getOutputStream());
                dout.writeUTF(message + "(发向全员)");
            }catch (Exception ex){
                contentArea.append("转发出错:"+ex.getMessage()+"\r\n");
            }
        }
    }
}

