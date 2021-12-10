import javax.sound.sampled.FloatControl;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务端处理
 */
public class TicTacToeServer {
    public static void main(String[] args) throws Exception {
        try (ServerSocket listener = new ServerSocket(58901)) {
            System.out.println("Tic Tac Toe Server is Running...");
            ExecutorService pool = Executors.newFixedThreadPool(200);
            Game game = new Game();
            //创建连接池对象
            while (true) {
                Socket accept = listener.accept();
                pool.execute(game.new Player(accept, 'X'));
                accept = listener.accept();
                pool.execute(game.new Player(accept, 'O'));
            }
        }
    }
}

class Game {

    // Board cells numbered 0-8, top to bottom, left to right; null if empty
    private Player[] board = new Player[9];
    Player currentPlayer = null;

    /**
     * 判断当前玩家是否胜利
     * @return
     */
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

    /**
     * 清空棋盘的标记
     */
    public void clearboard(){
        for(int i=0;i<9;i++) {
            board[i] = null;
        }
        currentPlayer = null;
    }

    /**
     * 判断棋盘是否还有空
     * @return
     */
    public boolean boardFilledUp() {
        for(int i=0;i<9;i++){
            if(board[i]==null) return false;
        }
        return true;
    }

    /**
     * 当前玩家进行下棋，即修改棋盘状态
     * @param location
     * @param player
     * @param gametype
     */
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
        //真人对战时，每个回合都需要切换当前玩家的状态为对手
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
        private Socket socket;
        private Scanner input;
        private PrintWriter output;
        private String userId="";
        private String password="";

        public Player(Socket socket,char mark) {
            this.socket = socket;
            this.mark = mark;
        }

        public void run() {
            try {
                System.out.println("player run....");
                System.out.println("run socket:"+socket);
                this.input = new Scanner(socket.getInputStream());
                this.output = new PrintWriter(socket.getOutputStream(), true);
                if(login()){ //判断是否登录，注册，退出
                    setup();
                    processCommands();
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 拦截用户进行身份和操作确认
         * 验证登录，首次登录返回游戏历史记录，并修改当前用户为在线状态，避免重复登录
         * 在个人中心和进行游戏前二次登录，采用token验证是否为已经收到Login Successful
         * 用户注册，查询数据库查是否保证数据唯一
         * 用户退出时，修改用户状态为下线
         * @return
         * @throws Exception
         */
        public boolean login() throws Exception {
            String requset = input.nextLine();
            System.out.println("Login requset:"+requset);
            if(requset.startsWith("Login Successful")){
                String[] split = requset.split(",");
                userId=split[1];
                gameType=split[2];
                return true;
            }
            else if(requset.startsWith("Login")) {
                String[] split = requset.split(",");
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
                        output.println("Login Successful");
                        sql="UPDATE user SET login=? WHERE userId=?";  //修改该用户在线状态
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);
                        preparedStatement.setString(1,"yes");
                        preparedStatement.setString(2,userId);
                        preparedStatement.executeUpdate();
                        queryHistory(userId);
                    }else output.println("user exist");
                } else {
                    output.println("Login Fail");
                }
                JDBCUtils.Close(connection);
            }else if(requset.startsWith("Register")){
                try{
                    String[] split = requset.split(":");
                    System.out.println("Register split:"+split[1]+" "+split[2]);
                    Connection connection = JDBCUtils.getConnection();
                    String sql="insert into user (userName,userId,password,login) VALUES (?,?,?,?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1,split[1]);
                    preparedStatement.setString(2,split[1]);
                    preparedStatement.setString(3,split[2]);
                    preparedStatement.setString(4,"no");
                    int i = preparedStatement.executeUpdate();
                    System.out.println("Register result:"+i);
                    connection.close();
                    output.println(((i>=1)?"Successful":"Fail"));
                }catch (SQLException e){
                    e.printStackTrace();
                    System.out.println("Register Fail 账号已存在");
                    output.println("Fail");
                }
            }else if(requset.startsWith("exit")) {
                Connection connection = JDBCUtils.getConnection();
                String []split = requset.split(",");
                String sql = "UPDATE user SET login=? WHERE userId=?"; //修改该用户在线状态
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, "no");
                preparedStatement.setString(2, split[1]);
                preparedStatement.executeUpdate();
                JDBCUtils.Close(connection);
            }
            return false;
        }

        /**
         * 根据用户id往数据库插入游戏记录
         * @param id
         */
        public void queryHistory(String id)  {
            try {
                Connection connection = JDBCUtils.getConnection();
                Statement statement = connection.createStatement();
                //发送历史记录
                String sql = "select gameTime,gameResult,opponent from game where userId = \'" + id + "\' ORDER BY gameTime DESC";
                ResultSet resultSet = statement.executeQuery(sql);
                String queryAnswer = "gameTime   gameResult" + "  opponent";
                while (resultSet.next()) {
                    queryAnswer = queryAnswer + resultSet.getString(1) + "   ";
                    String arg = resultSet.getString(2);
                    if (!arg.equals("yes")) arg += "  ";
                    queryAnswer += arg + "          "+resultSet.getString(3)+"        ";
                }
                System.out.println("sql queryAnswer2--history:" + queryAnswer);
                output.println(queryAnswer);
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
            if(gameResult.equals("VICTORY"))gameResult="yes";
            else if(gameResult.equals("DEFEAT"))gameResult="no";
            else if(gameResult.equals("TIE"))gameResult="tie";
            if(!(gameResult.equals("yes")||gameResult.equals("no")||gameResult.equals("tie"))){
                return ;
            }
            try {
                //保证用户号不为空
                if(this.userId.length()>0){
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
        public void robot(){
            output.println("WELCOME " + 'O');
            System.out.println("robot WELCOME " + 'O');
            int location=-1;
            location=findplace();
            board[location]=opponent;
            output.println("OPPONENT_MOVED "+ location);
            if (hasWinner()) {
                output.println("DEFEAT");
            } else if (boardFilledUp()) {
                output.println("TIE");
            }
        }
        /**
         *  人机对战中机器下棋策略
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
            output.println("WELCOME " + mark);
            System.out.println("setup WELCOME:" + mark);
            System.out.println("setup gameType:"+gameType);
            if(gameType.equals("1")){  //状态1进行人机对战
                currentPlayer = this;
                output.println("MESSAGE Your move");
                opponent=new Player(socket,mark == 'X' ? 'O' : 'X');
                opponent.opponent = currentPlayer;
            }else if(gameType.equals("2")){ //状态2进行真人对战
                  if(currentPlayer==null){
                    currentPlayer = this;
                    System.out.println("currentPlayer:"+currentPlayer);
                    output.println("MESSAGE Waiting for opponent to connect");
                } else {
                    opponent = currentPlayer;
                    opponent.opponent = this;
                    System.out.println("opponent:"+opponent);
                    opponent.output.println("MESSAGE Your move");
                }
            }
        }

        /**
         * 游戏进行处理阶段
         * 通过input监听客服端信息，判断接下的指令
         */
        private void processCommands()  {
            String command="";
            while (input.hasNextLine()) {
                command = input.nextLine();
                System.out.println("server processCommands:"+command);
                if (command.startsWith("QUIT")) {
                    String[] split = command.split(",");
                    clearboard();
                    insertHistory(split[1],this.userId);
                    return;
                } else if (command.startsWith("MOVE")) {
                    processMoveCommand(Integer.parseInt(command.substring(5)));
                }
            }
        }

        /**
         * 判断游戏状态
         * 状态1进行人机对战
         * 状态2进行真人对战
         * @param location
         */
        private void processMoveCommand(int location) {
            try {
                boolean game_run=true;
                move(location, this, gameType);
                output.println("VALID_MOVE");
                if(gameType.equals("2")){
                    opponent.output.println("OPPONENT_MOVED " + location);
                }
                if (hasWinner()) {
                    output.println("VICTORY");
                    if(gameType.equals("2")){
                        opponent.output.println("DEFEAT");
                    }
                    game_run=false;
                } else if (boardFilledUp()) {
                    output.println("TIE");
                    if(gameType.equals("2")){
                        opponent.output.println("TIE");
                    }
                    game_run=false;
                }
                if(game_run&&gameType.equals("1")){
                    robot();
                }
            } catch (IllegalStateException e) {
                output.println("MESSAGE " + e.getMessage());
            }
        }
    }
}