import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 服务端
 */
public class UDP_TicTacToeServer {
    //创建连接池对象
    public static void main(String[] args) throws Exception {

        byte[] buf  = new byte[1024];
        int serverPort=58901;
        int clientPort=58902;
        int newClientPort = 30000;  //记录客户端端口号
        int newServerPort = 31000;  //记录服务器对应的端口号
        InetAddress serverIp  = InetAddress.getByName("localhost");
        InetAddress clientIp  = InetAddress.getByName("localhost");
        DatagramSocket socket = null;
        DatagramPacket datagramPacket = null;
        try{
            System.out.println("Tic Tac Toe Server is Running...");
            ExecutorService pool = Executors.newFixedThreadPool(200);
            Game game = new Game();
            String request="";
            while (true) {
                socket = new DatagramSocket(serverPort,serverIp);
                datagramPacket=new DatagramPacket(new byte[1024],0,1024,clientIp,clientPort);
                socket.receive(datagramPacket);//第一次等待客户端发送UDP信息

                request = "client:"+new Integer(newClientPort)+":"+"server:"+new Integer(newServerPort);
                System.out.println("第一次发送:"+request);
                datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,clientIp,clientPort);
                socket.send(datagramPacket);  //第一次把指定的客服端端口号和服务器端口号发送给客户端
                DatagramSocket socket1 = new DatagramSocket(newServerPort);
                DatagramPacket datagramPacketNew1 = new DatagramPacket(buf,buf.length,clientIp,newClientPort);
                socket1.receive(datagramPacketNew1); //第2次等待客户端发送UDP信息
                pool.execute(game.new Player(datagramPacketNew1,socket1,'X'));
                System.out.println("第二次连接:");
                socket.close();
                socket = new DatagramSocket(serverPort,serverIp);
                newServerPort++;newClientPort--;
                datagramPacketNew1 = new DatagramPacket(buf,buf.length,clientIp,clientPort);
                socket.receive(datagramPacketNew1);  //第一次接受 申请连接
                System.out.println("第二次连接: 第一次接受 申请连接");
                request = "client:"+new Integer(newClientPort)+":"+"server:"+new Integer(newServerPort);
                datagramPacket=new DatagramPacket(request.getBytes(),0,request.getBytes().length,clientIp,clientPort);
                socket.send(datagramPacket); //把指定的客服端端口号和服务端端口号发送给客户端
                DatagramSocket socket2 = new DatagramSocket(newServerPort);
                DatagramPacket response2 = new DatagramPacket(new byte[1024],0,1024,clientIp,newClientPort);
                socket2.receive(response2); //第二次接受 申请开始
                pool.execute(game.new Player(response2,socket2,'O'));
                System.out.println("socket2");
                newServerPort++;newClientPort--;
                socket.close();
            }
        }catch(IOException e){
        }
    }
}

class Game {

    // Board cells numbered 0-8, top to bottom, left to right; null if empty
    private Player[] board = new Player[9];
    Player currentPlayer = null;
    Player save_currentPlayer = null;
    public boolean hasWinner() {
        return (board[0] != null && board[0] == board[1] && board[0] == board[2])
                || (board[3] != null && board[3] == board[4] && board[3] == board[5])
                || (board[6] != null && board[6] == board[7] && board[6] == board[8])
                || (board[0] != null && board[0] == board[3] && board[0] == board[6])
                || (board[1] != null && board[1] == board[4] && board[1] == board[7])
                || (board[2] != null && board[2] == board[5] && board[2] == board[8])
                || (board[0] != null && board[0] == board[4] && board[0] == board[8])
                || (board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }

    public void clearboard(){
        for(int i=0;i<9;i++) {
            board[i] = null;
        }
        if(currentPlayer!=null) {
            currentPlayer.opponent.opponent = null;
            currentPlayer.opponent = null;
        }
        currentPlayer = save_currentPlayer;
        save_currentPlayer = null;
    }

    public boolean boardFilledUp() {
        for(int i=0;i<9;i++){
            if(board[i]==null) return false;
        }
        return true;
    }

    public synchronized void move(int location, Player player, String gametype) {
        if(player != currentPlayer){
            throw new IllegalStateException("Not your trun");
        }
        if (board[location] != null) {
            throw new IllegalStateException("Cell already occupied");
        }else if (currentPlayer.opponent == null&&gametype.equals("2")) {
            throw new IllegalStateException("You don't have an opponent yet");
        }
        board[location] = currentPlayer;
        if(gametype.equals("2")) currentPlayer = currentPlayer.opponent;
    }

    /**
     * A Player is identified by a character mark which is either 'X' or 'O'. For
     * communication with the client the player has a socket and associated Scanner
     * and PrintWriter.
     */
    class Player implements Runnable {
        private char mark;
        private String gameType=null;
        private Player opponent;
        private String userId="";
        private String password="";
        private byte[] buf  = new byte[1024];
        private DatagramSocket socket1;
        private DatagramPacket datagramPacket;

        public Player(DatagramPacket datagramPacket,DatagramSocket socket1,char mark){
            this.datagramPacket=datagramPacket;
            this.mark = mark;
            this.socket1=socket1;
        }

        public void run() {
            try {
                System.out.println("player run....");
                if(login()) {
                    while (true) {
                        datagramPacket.setData(buf, 0, buf.length);
                        System.out.println("login socket1:" + socket1);
                        socket1.receive(datagramPacket);
                        String response = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), "UTF-8");
                        if (response.startsWith("start game")) {
                            String[] split = response.split(",");
                            userId = split[1];
                            gameType = split[2];
                            setup();
                            processCommands();
                        } else if (response.startsWith("exit")) {
                            System.out.println("login after response:" + response);
                            Connection connection = JDBCUtils.getConnection();
                            String[] a = response.split(",");
                            String sql = "UPDATE user SET login=? WHERE userId=?";
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, "no");
                            preparedStatement.setString(2, a[1]);
                            preparedStatement.executeUpdate();
                            JDBCUtils.Close(connection);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 验证登录，首次登录返回游戏历史记录
         * 在个人中心和进行游戏前二次登录，采用token验证是否为已经收到Login Successful
         * @return
         * @throws Exception
         */
        public boolean login() throws Exception {
            datagramPacket.setData(buf,0,buf.length);
            System.out.println("login socket1:"+socket1);
            socket1.receive(datagramPacket);
            String requset = "";
            String response = new String(datagramPacket.getData(),0,datagramPacket.getLength(),"UTF-8");
            System.out.println("Login response:"+response);
            if(response.startsWith("Login")) {
                String[] split = response.split(",");
                userId=split[1];
                password = split[2];
                System.out.println("Login userID:" + userId+" password:" + password);
                String queryAnswer = "";
                String login="";
                Connection connection = JDBCUtils.getConnection();
                Statement statement = connection.createStatement();
                String sql = "select password,login from user where userId = \'" + userId + "\'";
                System.out.println("sql:" + sql);
                ResultSet resultSet = statement.executeQuery(sql);
                System.out.println("sql resultSet:" + resultSet);
                while (resultSet.next()) {
                    queryAnswer = resultSet.getString(1);
                    login=resultSet.getString(2);
                    System.out.println("login:"+login);
                }
                System.out.println("sql queryAnswer1--password:" + queryAnswer);
                if (password.equals(queryAnswer)) {
                    if(login.equals("no")) {
                        requset="Login Successful";
                        datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                        socket1.send(datagramPacket);
                        sql="UPDATE user SET login=? WHERE userId=?";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);
                        preparedStatement.setString(1,"yes");
                        preparedStatement.setString(2,userId);
                        preparedStatement.executeUpdate();
                        queryHistory(userId);
                        return true;
                    }else {
                        requset="user exist";
                        datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                        socket1.send(datagramPacket);
                    }
                } else {
                    requset="Login Fail";
                    datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                    socket1.send(datagramPacket);
                }
                JDBCUtils.Close(connection);
            }else if(response.startsWith("Register")) {
                try {
                    String[] split = response.split(":");
                    System.out.println("Register split:" + split[1] + " " + split[2]);
                    Connection connection = JDBCUtils.getConnection();
                    String sql = "insert into user (userName,userId,password,login) VALUES (?,?,?,?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, split[1]);
                    preparedStatement.setString(2, split[1]);
                    preparedStatement.setString(3, split[2]);
                    preparedStatement.setString(4, "no");
                    int i = preparedStatement.executeUpdate();
                    System.out.println("Register result:" + i);
                    connection.close();
                    if (i >= 1) requset = "Successful";
                    else requset = "Fail";
                    datagramPacket.setData(requset.getBytes(), 0, requset.getBytes().length);
                    socket1.send(datagramPacket);
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Register Fail 账号已存在");
                    requset = "Fail";
                    datagramPacket.setData(requset.getBytes(), 0, requset.getBytes().length);
                    socket1.send(datagramPacket);
                }
            }
            return false;
        }

        public void queryHistory(String id)  {
            try {
                Connection connection = JDBCUtils.getConnection();
                Statement statement = connection.createStatement();
                //发送历史记录
                String sql = "select gameTime,gameResult from game where userId = \'" + id + "\' ORDER BY gameTime DESC";
                ResultSet resultSet = statement.executeQuery(sql);
                String queryAnswer = "gameTime   gameResult" + "               ";
                while (resultSet.next()) {
                    queryAnswer = queryAnswer + resultSet.getString(1) + "   ";
                    String arg = resultSet.getString(2);
                    if (!arg.equals("yes")) arg += "  ";
                    queryAnswer += arg + "                 ";
                }
                System.out.println("sql queryAnswer2--history:" + queryAnswer);
                datagramPacket.setData(queryAnswer.getBytes(),0,queryAnswer.getBytes().length);
                socket1.send(datagramPacket);
                System.out.println("历史记录**************************");
                JDBCUtils.Close(connection);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        /**
         * 往数据库插入游戏记录
         * @param gameResult
         */
        public void insertHistory(String gameResult,String id){
            //判断参数是否符合gameResult
            System.out.println("inserthiotory");
            if(gameResult.startsWith("VICTORY"))gameResult="yes";
            else if(gameResult.startsWith("DEFEAT"))gameResult="no";
            else if(gameResult.startsWith("TIE"))gameResult="tie";
            if(!(gameResult.equals("yes")||gameResult.equals("no")||gameResult.equals("tie"))){
                return ;
            }
            try {
                //保证用户号不为空
                System.out.println("this.userId*********************"+this.userId);
                if(this.userId.length()>0){
                    System.out.println("this.userId*********************");
                    Connection connection = JDBCUtils.getConnection();
                    SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm" );
                    Date date= new Date();
                    String gameTime = sdf.format(date);
                    String sql="insert into game (userId,gameTime,gameResult,opponent) VALUES (?,?,?,?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1,id);
                    preparedStatement.setString(2,gameTime);
                    preparedStatement.setString(3,gameResult);
                    preparedStatement.setString(4,gameType.equals("1")?"cs":"ps");
                    System.out.println("insert sql:"+"insert into game (userId,gameTime,gameResult,opponent) VALUES"+this.userId +","+gameTime+","+gameResult+","+(gameType.equals("1")?"cs":"ps"));
                    int i = preparedStatement.executeUpdate();
                    System.out.println("insertHistory result:"+i);
                    queryHistory(id);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        /**
         * 电脑人机下棋回合
         * 先发送一个WELCOME O字符串给客服端，便于客服端判断此次为电脑人机回合
         */
        public void robot() throws IOException{
            String requset=null;
            System.out.println("robot WELCOME " + 'O');
            int location=-1;
            location=findplace();
            board[location]=opponent;
            requset="OPPONENT_MOVED "+ location;
            datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
            socket1.send(datagramPacket);
            System.out.println("server-robot__send");
            if (hasWinner()) {
                requset="DEFEAT";
                datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                socket1.send(datagramPacket);
            } else if (boardFilledUp()) {
                requset="TIE";
                datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                socket1.send(datagramPacket);
            }
        }
        /**
         * \机器下棋策略
         */
        public int findplace() {
            Random random = new Random();
            int location;
            for (int i = 0; i < 9; i++) {
                if (board[i] == null) {
                    board[i] = opponent;
                    if (hasWinner()) {
                        board[i] = null;
                        return i;
                    }
                    else board[i] = null;
                }
            }
            for (int i = 0; i < 9; i++) {
                if (board[i] == null) {
                    board[i] = currentPlayer;
                    if (hasWinner()) {
                        board[i] = null;
                        return i;
                    } else board[i] = null;
                }
            }
            if(board[4]==null) return 4;
            else {
                while (true) {
                    location = random.nextInt(9);
                    if (board[location] == null) {
                        return location;
                    }
                }
            }
        }

        /**
         * 游戏正式开始前，客服端进行准备
         * @throws IOException
         */
        private void setup() throws IOException {
            String requset = null;
            requset="WELCOME " + mark;
            datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
            socket1.send(datagramPacket);
            System.out.println("setup WELCOME:" + mark);
            System.out.println("setup gameType:"+gameType);
            System.out.println("**************player:"+currentPlayer);
            if(gameType.equals("1")){
                save_currentPlayer=currentPlayer;
                currentPlayer = this;
                requset="MESSAGE Your move";
                datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                socket1.send(datagramPacket);
                opponent=new Player(datagramPacket,socket1,mark == 'X' ? 'O' : 'X');
                opponent.opponent = currentPlayer;
            }else if(gameType.equals("2")){
                if(currentPlayer==null){
                    currentPlayer = this;
                    System.out.println("currentPlayer:"+currentPlayer);
                    requset="MESSAGE Waiting for opponent to connect";
                    datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                    socket1.send(datagramPacket);
                    socket1.send(datagramPacket);
                } else {
                    opponent = currentPlayer;
                    opponent.opponent = this;
                    System.out.println("opponent:"+opponent);
                    requset="MESSAGE Your move";
                    opponent.datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                    opponent.socket1.send(opponent.datagramPacket);
                    opponent.socket1.send(opponent.datagramPacket);
                    System.out.println("opponent:"+opponent + requset);
                }
            }

        }

        /**
         * 游戏进行处理阶段
         * 通过input监听客服端信息，判断接下的指令
         */
        private void processCommands() throws IOException {
            String command="";
            while (true) {
                byte[] buf1=new byte[1024];
                DatagramPacket receive = new DatagramPacket(buf1,0,buf1.length);
                socket1.receive(receive);
                command = new String(receive.getData(),0,receive.getData().length);
                System.out.println("server processCommands:"+command);
                if (command.startsWith("QUIT")) {
                    String[] split = command.split(",");
                    clearboard();
                    insertHistory(split[1],this.userId);
                    return;
                } else if (command.startsWith("MOVE")) {
                    processMoveCommand(Integer.parseInt(command.charAt(5)+""));
                }
            }
        }

        /**
         * 判断游戏状态
         * @param location
         */
        private void processMoveCommand(int location) throws IOException {
            try {
                boolean game_run=true;
                String requset=null;
                move(location, this, gameType);
                requset="VALID_MOVE";
                datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                socket1.send(datagramPacket);
                socket1.send(datagramPacket);
                System.out.println("processMoveCommand VALID_MOVE send");
                if(gameType.equals("2")){
                    requset="OPPONENT_MOVED " + location;
                    opponent.datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                    opponent.socket1.send(opponent.datagramPacket);
                }
                if (hasWinner()) {
                    requset="VICTORY";
                    datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                    socket1.send(datagramPacket);
                    if(gameType.equals("2")){
                        requset="DEFEAT";
                        opponent.datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                        opponent.socket1.send(opponent.datagramPacket);
                    }
                    game_run=false;
                } else if (boardFilledUp()) {
                    requset="TIE";
                    datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                    socket1.send(datagramPacket);
                    if(gameType.equals("2")){
                        requset="TIE";
                        opponent.datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                        opponent.socket1.send(opponent.datagramPacket);
                    }
                    game_run=false;
                }
                if(game_run&&gameType.equals("1")){
                    robot();
                }
            } catch (IllegalStateException | IOException e){
                String requset=null;
                requset="MESSAGE " + e.getMessage();
                datagramPacket.setData(requset.getBytes(),0,requset.getBytes().length);
                socket1.send(datagramPacket);
            }
        }
    }
}
