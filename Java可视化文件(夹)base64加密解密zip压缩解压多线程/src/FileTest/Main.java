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
	 * �ַ�������
	 * @param str
	 * @return
	 */
    static public String EncodeBase64(String str){
        byte[] bytes = str.getBytes();
        String encode = Base64.getEncoder().encodeToString(bytes);
        return encode;
    }
    /**
     * �ַ�������
     * @param str
     * @return
     */
    static public  String DecodeBase64(String str){
        byte[] bytes = Base64.getDecoder().decode(str);
        String s = new String(bytes);
        return s;
    }
    /**
     * �ļ�����
     * @param file
     */
    static public void EncodeFile(){
        String fileToString = FileToString();
        //����
        String s = Base64Util.EncodeBase64(fileToString);
        //����ļ�������д��
        ReWriteInfoForFile(s);
        System.out.println("�ļ�"+pathString+"���ܺ�:"+"\n"+s);
        //����ļ�����׺��
        ChangeSufFileName();
    }
    /**
     * �ļ�����
     * @param file
     */
    static public void  DecodeFile( ){
        String fileToString = FileToString();
        String s = Base64Util.DecodeBase64(fileToString);
        //����ļ�������д��
        ReWriteInfoForFile(s);
        System.out.println("�ļ�"+pathString+"���ܺ�:"+"\n"+s);
        ChangeSufFileName();
    }
    /**
     * �޸��ļ�����׺��
     * @param file
     * @param isAdd
     */
    static public void ChangeSufFileName(){
        //�ļ������enc
    	File file=new File(pathString);
        String fileName = file.getName();
        if(isEncode){
            fileName=fileName.substring(0, fileName.length()-4)+".enc"+fileName.substring(fileName.length()-4);
        }
        else {
            fileName=fileName.replaceAll(".enc", "");
        }
        String fileParent = file.getParent();
        //�ļ������ļ���·��+���ļ���
        String newPathName=fileParent+'\\'+fileName;
        file.renameTo(new File(newPathName));   //�޸�����ԭ�ļ��ᱻɾ��
        System.out.println("�޸ĺ���ļ���Ϊ"+newPathName);
        //System.out.println("�ļ�:"+file.getName()+":"+file.exists());
    }
    
    /**
     * �ļ�ת�����ַ���
     * @param file
     * @return
     */
    static public String FileToString(){
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(pathString));
            StringBuilder stringBuilder=new StringBuilder();
            int length;
            byte[] buf = new byte[1024];
            //����һ�������ַ�����
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
     * �Ѽ��ܻ���ܺ����Ϣ����д���ļ�
     * @param file
     * @param str
     */
    public static void ReWriteInfoForFile(String str) {
        try {
        	File file= new File(pathString);
            if(!file.exists()) {
                file.createNewFile();
            }
            //����ļ�
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
            //����д��
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
     * �ݹ����or�����ļ����ļ���
     * @param Path
     * @param isEncode
     */
	static ExecutorService pool=Executors.newFixedThreadPool(10);
	
	static StringBuilder stringBuilder=new StringBuilder("");
    static public void RecursiveEncodeOrDecode(String Path,boolean isEncode){
        File folder = new File(Path);
        if(!folder.exists()){
            System.out.println("�ļ�"+Path+"������");
            stringBuilder.append("�ļ�:"+Path+"������\n");
            return ;
        }
        if(folder.isFile()) {
        	if(isEncode){
                System.out.println("�ļ�"+folder.getAbsolutePath()+"���м���");
//                Base64Util.EncodeFile(file);
                Base64Util base64Util=new Base64Util(folder.getAbsolutePath(), isEncode);
//                new Thread(base64Util).start();
                pool.submit(base64Util);
            } else {
                System.out.println("�ļ�"+folder.getAbsolutePath()+"���н���");
//                Base64Util.DecodeFile(file);
                Base64Util base64Util=new Base64Util(folder.getAbsolutePath(), isEncode);
//                new Thread(base64Util).start();
                pool.submit(base64Util);
            }
        }
        File[] listFiles = folder.listFiles();
        if(null==listFiles||listFiles.length==0){
            System.out.println("�ļ���"+Path+"Ϊ��");
            stringBuilder.append("�ļ�:"+Path+"Ϊ��\n");
            return ;
        }
        for(File file:listFiles) {
            if (file.isDirectory()) {
                System.out.println("�ļ���" + file.getAbsolutePath() + "�����ݹ�");
                RecursiveEncodeOrDecode(file.getAbsolutePath(),isEncode);
            } else {
                if(isEncode){
                    System.out.println("�ļ�"+file.getAbsolutePath()+"���м���");
//                    Base64Util.EncodeFile(file);
                    Base64Util base64Util=new Base64Util(file.getAbsolutePath(), isEncode);
//                    new Thread(base64Util).start();
                    pool.submit(base64Util);
                } else {
                    System.out.println("�ļ�"+file.getAbsolutePath()+"���н���");
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
//	    //ѹ��
//	    FileUtil.FileToZip(path);
//	    path=path+".zip";
//	    //��ѹ
//	    FileUtil.ZipToFile(path);
        //�ļ�/�ļ���·��
//        String Path="D:\\desktop\\Text";
//        //����
//        FileUtil.RecursiveEncodeOrDecode(Path,true);
//        String s = FileUtil.FileToString(new File(Path));
//        System.out.println();
//        //����
//        FileUtil.RecursiveEncodeOrDecode(Path,false);
//        String str = FileUtil.FileToString(new File(Path));
//    }
//	
	
}
