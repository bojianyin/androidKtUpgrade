import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        File file = new File("/Users/macbookpro/Downloads/Bitbrex-Android-1204/bigclient-android-Bitbrex_master_5.3.2_5340_202312041507@029764e58f6/app/src/main/java/com/yjkj/chainup/new_version/activity/CashFlowDetailActivity.kt");
//        File xmlfile = new File("/Users/macbookpro/Downloads/Bitbrex-Android-1204/bigclient-android-Bitbrex_master_5.3.2_5340_202312041507@029764e58f6/app/src/main/res/layout/activity_cashflow_detail.xml");
//        String content = readFileToString(file);
//        readFileLine(file);
//        System.out.println(content);
//        String[] ids = getIdStringByXml(xmlfile);
//        for(String item : ids){
//            System.out.println(item);
//        }
//        System.out.println(ids.length);

//        System.out.println(converId("activity_cashflow_detail"));

//        scanSingleFile(file);
    }


    public static void scanSingleFile(File file){
        Pattern pattern = Pattern.compile(".+[activity|Activity]{1}");
        boolean isActivityFile = pattern.matcher(file.getName()).matches();
        if(isActivityFile){
            System.out.println("Found activity file");
            String layoutName = getLayoutNameByFile(file);

            File layoutFile = findLayoutFile(file,layoutName);
            System.out.println(layoutFile.getAbsoluteFile());
            String[] ids = getIdStringByXml(layoutFile);

            StringBuilder sb = new StringBuilder();
            readFileLine(file, new ILine() {
                @Override
                public void line(String lineContent) {
                    boolean isExitId = false;
                    for(String id : ids){
                        if(lineContent.contains(id)){
                            lineContent = lineContent.replaceAll(id,"binding?."+converId(id));
                        }
                    }
                    sb.append(lineContent+"\n");
                }

                @Override
                public void layoutString(String layoutString) {

                }
            });

            System.out.println(sb.toString());
            writeFile(sb.toString(),file);
        }
    }

    public static String converId(String id){
        String[] idAry = id.split("_");
        StringBuilder sb = new StringBuilder();
        boolean isFirst= true;
        for(String item : idAry){
            if(isFirst) {
                sb.append(item);
                isFirst = false;
            }else{
                String firstWord = new String(new char[]{item.charAt(0)}).toUpperCase();
                sb.append(firstWord+item.substring(1,item.length()));
            }
        }
        return sb.toString();
    }


    public static String getLayoutNameByFile(File file){
        final String[] layoutName = {""};
        readFileLine(file, new ILine() {
            @Override
            public void line(String lineContent) {

            }

            @Override
            public void layoutString(String layoutString) {
//                    System.out.println(layoutString);
                layoutName[0] = layoutString;
            }
        });
        return layoutName[0];
    }

    public static File findLayoutFile(File file,String layoutName){
        File pFile = file.getParentFile();
        if(pFile==null) return null;
        File[] files = pFile.listFiles();
        for(File item:files){
            if(item.isDirectory() && item.getName().equals("res")){
                return new File(new File(item,"layout"),layoutName+".xml");
            }
        }
        return findLayoutFile(pFile,layoutName);
    }





    public static String readFileToString(File file) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        FileInputStream fileInputStream = null;
        try{
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length=fileInputStream.read(buffer))!=-1){
                arrayOutputStream.write(buffer,0,length);
            }
            arrayOutputStream.flush();
            arrayOutputStream.close();
            fileInputStream.close();
            return arrayOutputStream.toString(StandardCharsets.UTF_8);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public static void readFileLine(File file,ILine iLine){
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                iLine.line(line);
                Pattern pattern = Pattern.compile(".*R\\.layout\\.[0-9|a-zA-Z]{1}+.*");
                if(pattern.matcher(line).matches()){
                    iLine.layoutString(line.trim().replaceAll(".*R\\.layout\\.","").trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] getIdStringByXml(File file){
        if(!file.getName().contains("xml")){
            return new String[]{};
        }else{
            List<String> idStrings = new ArrayList<>();
            readFileLine(file, new ILine() {
                @Override
                public void line(String lineContent) {
                    Pattern pattern = Pattern.compile("^.+android:id=\"@\\+id/[0-9|a-zA-Z|_]+\".*$");
                    Matcher matcher = pattern.matcher(lineContent);
                    if(matcher.matches()){
//                        System.out.println(lineContent.trim());
                        idStrings.add(lineContent.trim().replace("android:id=\"@+id/","").replace("\"",""));
                    }
                }

                @Override
                public void layoutString(String layoutString) {

                }
            });
            return idStrings.toArray(new String[0]);
        }

    }


    public static void writeFile(String content,File file){
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    interface ILine{
        void line(String lineContent);
        void layoutString(String layoutString);
    }

}