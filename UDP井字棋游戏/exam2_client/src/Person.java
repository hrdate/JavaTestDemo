import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 个人中心窗口
 * 用户游戏记录
 * 选择游戏类型，人机对战，真人对战
 * 退出游戏
 */
public class Person implements Runnable{
    private ExecutorService pool = Executors.newFixedThreadPool(200);
    private JFrame jFrame;
    private String userId="";
    private String password="";
    private String token="";
    private JTextArea textArea=null;
    public static String history="";
    public  static String response="";
    public  static String request="";
    public byte[] buf = new byte[1024];
    private int clientPort=58903;
    private int serverPort=58901;
    private InetAddress serverIp=null; //localhost
    public DatagramSocket socket=null;  //16666
    private DatagramPacket datagramPacket=null;


    /**
     * 根据用Login登录窗口首次成功登录后获取的参数，进入到个人中心窗口的初始化
     * @param userId 用户id
     * @param password 用户密码
     * @param token 用户首次登录后获得的密钥
     * @param history 用户的游戏历史记录
     * @throws IOException
     */
    public Person(DatagramSocket socket,int clientPort,InetAddress serverIp,int serverPort,String userId, String password,String token,String history) throws IOException {
        this.socket=socket;
        System.out.println("Person socket:"+this.socket);
        this.clientPort=clientPort;
        this.serverIp=serverIp;
        this.serverPort=serverPort;
        this.userId=userId;
        this.password=password;
        this.token=token;
        Person.history=history;
        jFrame=new JFrame();
        jFrame.setTitle("个人中心窗口");
        Container con =jFrame.getContentPane();
        con.setLayout(new FlowLayout());
        JPanel jp1=new JPanel();
        JLabel jl1=new JLabel("用户号:"+userId);
        JPanel jp2=new JPanel();
        JButton jbt1=new JButton("人机对战");
        jFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
          public void windowClosing(WindowEvent e) {
              // 此处加入操作动作
                try {
                    request="exit,"+userId;
                    datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,serverIp,serverPort);
                    socket.send(datagramPacket);
                    System.out.println("结束此次登录");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
          }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        jbt1.addActionListener(new ActionListener() {  //人机对战
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UDP_TicTacToeClient client = new UDP_TicTacToeClient(socket,clientPort,serverIp,serverPort,userId+",1");
                    pool.execute(client);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        JButton jbt2=new JButton("结束登录");
        jbt2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    request="exit,"+userId;
                    datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,serverIp,serverPort);
                    socket.send(datagramPacket);
                    System.out.println("结束此次登录");
                    jFrame.dispose();
                    System.exit(0);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });
        JPanel jp3=new JPanel();
        JButton jbt3=new JButton("刷新历史");
        jbt3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(UDP_TicTacToeClient.history.length()<=0) UDP_TicTacToeClient.history=Person.history;
                Person.history= UDP_TicTacToeClient.history;
                textArea.setText(Person.history);
            }
        });
        JButton jbt4 = new JButton("真人对战");
        jbt4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UDP_TicTacToeClient client = new UDP_TicTacToeClient(socket,clientPort,serverIp,serverPort,userId+",2");
                    pool.execute(client);
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
        });
        //添加到当前窗口容器
        con.add(jp1.add(jl1));
        jp2.add(jbt1);jp2.add(jbt2);
        con.add(jp2);
        jp3.add(jbt4);jp3.add(jbt3);
        con.add(jp3);
        JPanel jp4=new JPanel();
        textArea = new JTextArea(17, 18);
        // 设置自动换行
        textArea.setLineWrap(true);
        //不可编辑
        textArea.setEditable(false);
        textArea.setText(history);
        // 添加到内容面板
        jp4.add(textArea);
        JScrollPane jsp = new JScrollPane(textArea);
        jp4.add(jsp);
        //默认的设置是超过文本框才会显示滚动条,以下设置让滚动条一直显示
        jsp.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        con.add(jp4);
        jFrame.setSize(250, 450);    //设置窗体大小
        jFrame.setLocationRelativeTo(null); //设置窗口居中
        jFrame.setResizable(false);  //窗体大小设置为不可改
        jFrame.setVisible(true);  //窗体设置为可见
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void run() {

    }
}
