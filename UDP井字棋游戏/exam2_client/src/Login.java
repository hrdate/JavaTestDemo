import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;

/**
 * 登录窗口
 * 登录，注册
 * 同个用户账号只能在线一个
 */
class Login   {
    public JFrame frame=new JFrame();
    public  static String response="";
    public  static String request="";
    public byte[] buf = new byte[1024];
    private int serverPort=58901;
    private int clientPort=58902;
    private InetAddress serverIp=null; //localhost
    private DatagramSocket socket=null;  //16666
    private DatagramPacket datagramPacket=null;

    public Login() {
        // TODO 自动生成的构造函数存根
        frame.setTitle("客服端登入窗口");
        Container con =frame.getContentPane();
        con.setLayout(new FlowLayout());
        //用户名
        JPanel jp1=new JPanel();
        JLabel jl1=new JLabel("账号");
        JTextField jtf1=new JTextField(15);
        //密码
        JPanel jp2=new JPanel();
        JLabel jl2=new JLabel("密码");
        JPasswordField jpf2 = new JPasswordField(15);
        //登入取消按钮
        JPanel jp5=new JPanel();
        JButton jbt1=new JButton("登入");
        JButton jbt2=new JButton("取消");
        JButton jbt3=new JButton("注册");
        //用户名
        jp1.add(jl1);
        jp1.add(jtf1);
        //密码
        jpf2.setEchoChar('*');  //用*显示密码框输入的数据
        jp2.add(jl2);
        jp2.add(jpf2);
        //登入取消按钮
        jp5.add(jbt1);
        jp5.add(jbt2);
        jp5.add(jbt3);
        //添加到当前窗口容器
        con.add(jp1);
        con.add(jp2);
        con.add(jp5);
        frame.setSize(300, 300);    //设置窗体大小
        frame.setLocationRelativeTo(null); //设置窗口居中
        frame.setResizable(false);  //窗体大小设置为不可改
        frame.setVisible(true);  //窗体设置为可见
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //光标聚焦在用户框中
        jtf1.requestFocus();
        //为登入按钮添加监听器
        jbt1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO 自动生成的方法存根
                String userId=jtf1.getText(); //获取用户名
                String password=new String(jpf2.getPassword()); //获取密码框
                try {
                    serverIp=InetAddress.getByName("localhost");
                    //创建一个本地任意UDP套接字
                    socket=new DatagramSocket(clientPort);
                    request="request login";
                    datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,serverIp,serverPort);
                    socket.send(datagramPacket);  //第一次发送申请连接
                    datagramPacket=new DatagramPacket(buf,0,1024,serverIp,serverPort);
                    socket.receive(datagramPacket);
                    response=new String(datagramPacket.getData(), 0,datagramPacket.getLength());
                    System.out.println("1:first Login response:"+response);
                    String[] split = response.split(":");
                    socket.close(); //断开第一次UDP服务端
                    int newClientPort = Integer.parseInt(split[1]);  //获取新的客户端端口号
                    int newServerPort = Integer.parseInt(split[3]);  //获取新的服务端端口号
                    System.out.println("clientPort:"+newClientPort+" serverPort:"+newServerPort);
                    socket=new DatagramSocket(newClientPort);
                    request="Login,"+userId+","+password;
                    datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,serverIp,newServerPort);
                    socket.send(datagramPacket);   //第二次发送申请开始
                    socket.send(datagramPacket);  //第三次发送申请登录
                    System.out.println("2:first Login userID,"+userId+" Login password,"+password);
                    datagramPacket=new DatagramPacket(buf,0,1024);
                    socket.receive(datagramPacket);
                    response=new String(datagramPacket.getData(), 0,datagramPacket.getLength());
                    System.out.println("3:first Login response:"+response);
                    if(response.equals("Login Successful")) {
                        frame.setVisible(false);  //窗体设置为可不见
                        datagramPacket=new DatagramPacket(buf,1024);
                        socket.receive(datagramPacket);
                        String history=new String(datagramPacket.getData(), 0,datagramPacket.getLength());
                        System.out.println("Login socket:"+socket);
                        Person person = new Person(socket,newClientPort,serverIp,newServerPort,userId,password,response,history);
                        new Thread(person).start();
                    }
                    else if(response.equals("user exist")){
                        JOptionPane.showConfirmDialog(null, "此用户已经登录！",
                                "提示",JOptionPane.DEFAULT_OPTION);
                    }
                    else{
                        JOptionPane.showConfirmDialog(null, "用户名或密码错误！",
                                "提示",JOptionPane.DEFAULT_OPTION);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        });
        jbt2.addActionListener(new ActionListener() { //为取消按钮添加监听器
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    System.exit(0);
                } catch (Exception e2) {
                }
            }
        });
        jbt3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    serverIp=InetAddress.getByName("localhost");
                    //创建一个本地任意UDP套接字
                    socket=new DatagramSocket(clientPort);
                    request="request login";
                    datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,serverIp,serverPort);
                    socket.send(datagramPacket);  //第一次发送申请连接
                    datagramPacket=new DatagramPacket(buf,0,1024,serverIp,serverPort);
                    socket.receive(datagramPacket);
                    response=new String(datagramPacket.getData(), 0,datagramPacket.getLength());
                    System.out.println("1:first Login response:"+response);
                    String[] split = response.split(":");
                    socket.close(); //断开第一次UDP服务端
                    int newClientPort = Integer.parseInt(split[1]);  //获取新的客户端端口号
                    int newServerPort = Integer.parseInt(split[3]);  //获取新的服务端端口号
                    System.out.println("clientPort:"+newClientPort+" serverPort:"+newServerPort);
                    socket=new DatagramSocket(newClientPort);
                    request="Register:"+ jtf1.getText()+":"+new String(jpf2.getPassword());
                    datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,serverIp,newServerPort);
                    socket.send(datagramPacket);
                    socket.send(datagramPacket);
                    datagramPacket=new DatagramPacket(buf,1024);
                    socket.receive(datagramPacket);
                    response=new String(datagramPacket.getData(), 0,datagramPacket.getLength());
                    System.out.println("Register response:"+response);
                    if(response.equals("Successful")){
                        JOptionPane.showConfirmDialog(null, "注册成功！",
                                "提示",JOptionPane.DEFAULT_OPTION);
                    }else{
                        JOptionPane.showConfirmDialog(null, "注册失败！",
                                "提示",JOptionPane.DEFAULT_OPTION);
                    }
                }
                catch (IOException ioException) {
                    ioException.printStackTrace();
                    try {
                        JOptionPane.showConfirmDialog(null, "注册失败！账号已存在",
                                "提示",JOptionPane.DEFAULT_OPTION);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
        });
    }
}