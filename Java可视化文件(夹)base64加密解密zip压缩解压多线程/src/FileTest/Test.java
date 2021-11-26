package FileTest;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import java.awt.Choice;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Color;
import javax.swing.JLabel;

import FileTest.Main;

public class Test {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test window = new Test();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Test() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()  {
		String fileName[]={""};
		frame = new JFrame();
		frame.getContentPane().setFont(new Font("宋体", Font.PLAIN, 20));
		frame.setBounds(100, 100, 556, 445);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		
		JLabel lab = new JLabel("\u6587\u4EF6\u540D\uFF1A");
		lab.setFont(new Font("宋体", Font.PLAIN, 20));
		lab.setBounds(33, 10, 591, 25);
		frame.getContentPane().add(lab);
		
		JTextArea textArea = new JTextArea();
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textArea.setForeground(Color.BLACK);
		textArea.setEditable(false);
		textArea.setBounds(33, 39, 480, 272);
		frame.getContentPane().add(textArea);
		
		/**
		 * 加密
		 */
		JButton btn3 = new JButton("\u52A0\u5BC6");
		btn3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				long startTime = System.currentTimeMillis(); 
				//do something
				try {
					FileUtil.RecursiveEncodeOrDecode(fileName[0],true);
					textArea.setText(fileName[0]+"加密成功!");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				long endTime = System.currentTimeMillis(); 
		    	System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
			}
		});
		btn3.setFont(new Font("宋体", Font.PLAIN, 20));
		btn3.setBounds(308, 321, 99, 33);
		frame.getContentPane().add(btn3);
		
		/**
		 * 解密
		 */
		JButton btn33 = new JButton("\u89E3\u5BC6");
		btn33.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				long startTime = System.currentTimeMillis(); 
				//do something
				try {
					FileUtil.RecursiveEncodeOrDecode(fileName[0],false);
					textArea.setText(fileName[0]+"解密成功!");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				long endTime = System.currentTimeMillis(); 
		    	System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
			}
		});
		btn33.setFont(new Font("宋体", Font.PLAIN, 20));
		btn33.setBounds(308, 364, 99, 33);
		frame.getContentPane().add(btn33);
		
		
		
		JButton btn1 = new JButton("\u6253\u5F00\u6587\u4EF6");
		
		/**
		 * 打开文件后显示文件内容
		 */
		btn1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser jfc =new JFileChooser("D:\\desktop\\Text");
				jfc.showDialog(btn1, null);
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jfc.showOpenDialog(null);
				File file=jfc.getSelectedFile();
				fileName[0] = file.getAbsolutePath();
				lab.setText(fileName[0]);
				System.out.println(fileName[0]);
				//原文件的输出流
				FileInputStream fileInputStream = null;
				try {
					fileInputStream = new FileInputStream(file);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		        StringBuilder textContent=new StringBuilder();
		        int length;
		        try {
					while((length=fileInputStream.read())!=-1){
					    textContent.append((char)(length));
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				textArea.setText(textContent.toString());
			}
		});
		btn1.setFont(new Font("宋体", Font.PLAIN, 20));
		btn1.setBounds(103, 321, 149, 33);
		frame.getContentPane().add(btn1);
		
		JButton btn11 = new JButton("\u6253\u5F00\u6587\u4EF6\u5939");
		btn11.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser jfc =new JFileChooser("D:\\desktop\\Text");
				jfc.showDialog(btn1, null);
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.showOpenDialog(null);
				File file=jfc.getSelectedFile();
				fileName[0] = file.getAbsolutePath();
				lab.setText(fileName[0]);
				System.out.println(fileName[0]);
				textArea.setText("文件夹:"+fileName[0]);
			}
		});
		btn11.setFont(new Font("宋体", Font.PLAIN, 20));
		btn11.setBounds(103, 364, 149, 33);
		frame.getContentPane().add(btn11);
		
		
	}
}
