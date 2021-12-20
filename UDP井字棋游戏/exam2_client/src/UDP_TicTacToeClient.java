import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.*;

/**
 * 游戏窗口，人机对战，真人对战
 */
public class UDP_TicTacToeClient implements  Runnable{

    private JFrame frame = new JFrame("Tic Tac Toe");
    private JLabel messageLabel = new JLabel("...");
    private Square[] board = new Square[9];
    private Square currentSquare;
    private String token="";
    private int serverPort=58901;
    private int clientPort=58902;
    private InetAddress serverIp=null; //localhost
    private DatagramSocket socket=null;  //16666
    public byte[]buf = new byte[1024];
    private DatagramPacket datagramPacket=null;
    public static  String history="";
    public  static String response="";
    public  static String request="";

    /**
     *
     * @param token 游戏开始前初始化，个人中心person窗口传递参数
     * @throws Exception
     */
    public UDP_TicTacToeClient(DatagramSocket socket, int clientPort, InetAddress serverIp, int serverPort, String token) throws Exception {
        this.socket=socket;
        System.out.println("UDP_TicTacToeClient socket:"+socket);
        this.clientPort=clientPort;
        this.serverIp=serverIp;
        this.serverPort=serverPort;
        this.token=token;
        messageLabel.setBackground(Color.lightGray);
        frame.getContentPane().add(messageLabel, BorderLayout.SOUTH);

        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    request="MOVE " + j;
                    datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,serverIp,serverPort);
                    try {
                        socket.send(datagramPacket);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    System.out.println("mousePressed MOVE "+j);
                }
            });
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(320, 320);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    /**
     * The main thread of the client will listen for messages from the server. The
     * first message will be a "WELCOME" message in which we receive our mark. Then
     * we go into a loop listening for any of the other messages, and handling each
     * message appropriately. The "VICTORY", "DEFEAT", "TIE", and
     * "OTHER_PLAYER_LEFT" messages will ask the user whether or not to play another
     * game. If the answer is no, the loop is exited and the server is sent a "QUIT"
     * message.
     */
    public void play() throws Exception {
        try {
            String[] split = token.split(",");
            request="start game,"+split[0]+","+split[1];
            datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,serverIp,serverPort);
            try {
                socket.send(datagramPacket);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            System.out.println("first game output:"+token);
            datagramPacket=new DatagramPacket(buf,0,1024,serverIp,serverPort);
            socket.receive(datagramPacket);
            response=new String(datagramPacket.getData(), 0,datagramPacket.getLength());
            System.out.println("first play response:"+response);
            char mark = response.charAt(8);
            char opponentMark = mark == 'X' ? 'O' : 'X';
            frame.setTitle("Tic Tac Toe: Player " + mark);
            while (true) {
                datagramPacket=new DatagramPacket(buf,0,1024,serverIp,serverPort);
                socket.receive(datagramPacket);
                response=new String(datagramPacket.getData(), 0,datagramPacket.getLength());
                System.out.println("client:"+response);
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setText(mark);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    System.out.println(response);
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setText(opponentMark);
                    board[loc].repaint();
                    messageLabel.setText("Opponent moved, your turn");
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                } else if (response.startsWith("VICTORY")) {
                    JOptionPane.showMessageDialog(frame, "Winner Winner");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    JOptionPane.showMessageDialog(frame, "Sorry you lost");
                    break;
                } else if (response.startsWith("TIE")) {
                    JOptionPane.showMessageDialog(frame, "Tie");
                    break;
                } else if (response.startsWith("OTHER_PLAYER_LEFT")) {
                    JOptionPane.showMessageDialog(frame, "Other player left");
                    break;
                }
            }
            request="QUIT,"+response;
            datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,serverIp,serverPort);
            try {
                socket.send(datagramPacket);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            //每次游戏结束后，都申请获取当前用户的游戏记录，并存储在静态history中
            datagramPacket=new DatagramPacket(buf,0,1024,serverIp,serverPort);
            socket.receive(datagramPacket);
            response=new String(datagramPacket.getData(), 0,datagramPacket.getLength());
            history=response;
            System.out.println("游戏窗口历史1："+history);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            frame.dispose();
        }
    }

    @Override
    public void run() {
        try {
            play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 三字棋为3*3的棋盘，一共9个格子，每个格子都是一个Square容器
     */
    static class Square extends JPanel {
        private static final long serialVersionUID = 1L;
        JLabel label = new JLabel();

        public Square() {
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            label.setFont(new Font("Arial", Font.BOLD, 40));
            add(label);
        }

        public void setText(char text) {
            label.setForeground(text == 'X' ? Color.BLUE : Color.RED);
            label.setText(text + "");
        }
    }

    public static void main(String[] args) throws Exception {
        Login login = new Login();
    }
}

