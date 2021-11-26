package FileTest;


import java.io.*;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.ibm.icu.impl.coll.CollationWeights;

class Base64Util implements Runnable{
	/**
	 * 
	 */
	static boolean isEncode;
	static String pathString="";
	public Base64Util(String path, boolean T) {
		// TODO Auto-generated constructor stub
		pathString=path;
		isEncode=T;
	}
	/**
	 * 字符串加密
	 * @param str
	 * @return
	 */
    static public String EncodeBase64(String str){
        byte[] bytes = str.getBytes();
        String encode = Base64.getEncoder().encodeToString(bytes);
        return encode;
    }
    /**
     * 字符串解密
     * @param str
     * @return
     */
    static public  String DecodeBase64(String str){
        byte[] bytes = Base64.getDecoder().decode(str);
        String s = new String(bytes);
        return s;
    }
    /**
     * 文件加密
     * @param file
     */
    static public void EncodeFile(){
        String fileToString = FileToString();
        //加密
        String s = Base64Util.EncodeBase64(fileToString);
        //清空文件并重新写入
        ReWriteInfoForFile(s);
        System.out.println("文件"+pathString+"加密后:"+"\n"+s);
        //添加文件名后缀名
        ChangeSufFileName();
    }
    /**
     * 文件加密
     * @param file
     */
    static public void  DecodeFile( ){
        String fileToString = FileToString();
        String s = Base64Util.DecodeBase64(fileToString);
        //清空文件并重新写入
        ReWriteInfoForFile(s);
        System.out.println("文件"+pathString+"解密后:"+"\n"+s);
        ChangeSufFileName();
    }
    /**
     * 修改文件名后缀名
     * @param file
     * @param isAdd
     */
    static public void ChangeSufFileName(){
        //文件名后加enc
    	File file=new File(pathString);
        String fileName = file.getName();
        if(isEncode){
            fileName=fileName.substring(0, fileName.length()-4)+".enc"+fileName.substring(fileName.length()-4);
        }
        else {
            fileName=fileName.replaceAll(".enc", "");
        }
        String fileParent = file.getParent();
        //文件所在文件夹路径+新文件名
        String newPathName=fileParent+'\\'+fileName;
        file.renameTo(new File(newPathName));   //修改名后，原文件会被删除
        System.out.println("修改后的文件名为"+newPathName);
        //System.out.println("文件:"+file.getName()+":"+file.exists());
    }
    
    /**
     * 文件转化成字符串
     * @param file
     * @return
     */
    static public String FileToString(){
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(pathString));
            StringBuilder stringBuilder=new StringBuilder();
            int length;
            byte[] buf = new byte[1024];
            //返回一个整型字符数据
            while((length=fileInputStream.read(buf))!=-1){
                stringBuilder.append(new String(buf,0,length));
            }
            String fileTextString = stringBuilder.toString();
            fileInputStream.close();
            return fileTextString;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 把加密或解密后的信息重新写入文件
     * @param file
     * @param str
     */
    public static void ReWriteInfoForFile(String str) {
        try {
        	File file= new File(pathString);
            if(!file.exists()) {
                file.createNewFile();
            }
            //清空文件
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
            //重新写入
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bytes = str.getBytes();
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(isEncode) {
			EncodeFile();
		}
		else {
			DecodeFile();
		}
		
	}
}
class FileUtil {
    /**
     * 递归加密or解密文件或文件夹
     * @param Path
     * @param isEncode
     */
	static ExecutorService pool=Executors.newFixedThreadPool(10);
	
	static StringBuilder stringBuilder=new StringBuilder("");
    static public void RecursiveEncodeOrDecode(String Path,boolean isEncode){
        File folder = new File(Path);
        if(!folder.exists()){
            System.out.println("文件"+Path+"不存在");
            stringBuilder.append("文件:"+Path+"不存在\n");
            return ;
        }
        if(folder.isFile()) {
        	if(isEncode){
                System.out.println("文件"+folder.getAbsolutePath()+"进行加密");
//                Base64Util.EncodeFile(file);
                Base64Util base64Util=new Base64Util(folder.getAbsolutePath(), isEncode);
//                new Thread(base64Util).start();
                pool.submit(base64Util);
            } else {
                System.out.println("文件"+folder.getAbsolutePath()+"进行解密");
//                Base64Util.DecodeFile(file);
                Base64Util base64Util=new Base64Util(folder.getAbsolutePath(), isEncode);
//                new Thread(base64Util).start();
                pool.submit(base64Util);
            }
        }
        File[] listFiles = folder.listFiles();
        if(null==listFiles||listFiles.length==0){
            System.out.println("文件夹"+Path+"为空");
            stringBuilder.append("文件:"+Path+"为空\n");
            return ;
        }
        for(File file:listFiles) {
            if (file.isDirectory()) {
                System.out.println("文件夹" + file.getAbsolutePath() + "继续递归");
                RecursiveEncodeOrDecode(file.getAbsolutePath(),isEncode);
            } else {
                if(isEncode){
                    System.out.println("文件"+file.getAbsolutePath()+"进行加密");
//                    Base64Util.EncodeFile(file);
                    Base64Util base64Util=new Base64Util(file.getAbsolutePath(), isEncode);
//                    new Thread(base64Util).start();
                    pool.submit(base64Util);
                } else {
                    System.out.println("文件"+file.getAbsolutePath()+"进行解密");
//                    Base64Util.DecodeFile(file);
                    Base64Util base64Util=new Base64Util(file.getAbsolutePath(), isEncode);
//                    new Thread(base64Util).start();
                    pool.submit(base64Util);
                }
            }
        }
    }

	
}

public class Main {

//	public static void main(String[] args) {
//		String path="D:\\desktop\\Text\\123.txt";
//	    path="D:\\desktop\\Text\\123";
//	    //压缩
//	    FileUtil.FileToZip(path);
//	    path=path+".zip";
//	    //解压
//	    FileUtil.ZipToFile(path);
        //文件/文件夹路径
//        String Path="D:\\desktop\\Text";
//        //加密
//        FileUtil.RecursiveEncodeOrDecode(Path,true);
//        String s = FileUtil.FileToString(new File(Path));
//        System.out.println();
//        //解密
//        FileUtil.RecursiveEncodeOrDecode(Path,false);
//        String str = FileUtil.FileToString(new File(Path));
//    }
//	
	
}
