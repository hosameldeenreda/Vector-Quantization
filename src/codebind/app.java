package codebind;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;
import javax.swing.JOptionPane;
public class app {
    private JTextField path;
    private JButton decompressbtn;
    private JButton compressbtn;
    private JPanel jPanel;
    private JTextField width_txt;
    private JTextField height_txt;
    private JTextField levels_txt;
    public static int[][] avg(Vector<int[][]> data,int w,int l){
        int[][] r=new int[w][l];
        if(data.size()!=0) {
            for (int j = 0; j < w; j++) {
                for (int k = 0; k < l; k++) {
                    for (int i = 0; i < data.size(); i++) {
                        r[j][k] += data.get(i)[j][k];
                    }
                    r[j][k] /= data.size();
                }
            }
        }
        return r;
    }
    public static Vector<int[][]> divideImage(int data[][],int w,int l){
        Vector<int[][]> r=new Vector<int[][]>();
        int wb=0,lb=0;
        for(int k=0;k<data.length;k+=w){
            for (int u = 0; u < data[0].length; u+=l) {
                int[][] temp=new int[w][l];
                for (int i = 0; i < w;i++) {
                    for (int j = 0; j < l;j++) {
                        temp[i][j] = data[i+wb][j+lb];
                    }
                }
                lb+=l;
                r.add(temp);
            }
            lb=0;
            wb+=w;
        }
        return r;
    }
    public static int[][] readGreyImage(String path){
        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));
            int height=img.getHeight();
            int width=img.getWidth();
            int[][] imagePixels=new int[width][height];
            for(int x=0;x<width;x++){
                for(int y=0;y<height;y++){
                    int pixel=img.getRGB(x,y);
                    int red=(pixel  & 0x00ff0000) >> 16;
                    imagePixels[x][y]=red;
                }
            }
            return imagePixels;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return null;
        }

    }
    public static void writeImage(int[][] imagePixels,String outPath){
        BufferedImage image = new BufferedImage(imagePixels.length, imagePixels[0].length, BufferedImage.TYPE_INT_RGB);
        for (int y= 0; y < imagePixels.length; y++) {
            for (int x = 0; x < imagePixels[y].length; x++) {
                int value =-1 << 24;
                value= 0xff000000 | (imagePixels[y][x]<<16) | (imagePixels[y][x]<<8) | (imagePixels[y][x]);
                image.setRGB(y, x, value);
            }
        }
        File ImageFile = new File(outPath);
        try {
            ImageIO.write(image, "jpg", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int diffrence(int x1[][],int x2[][]){
        int dif=0;
        for(int i=0;i<x1[0].length;i++)
            for(int j=0;j<x1.length;j++) {
                dif+=(int) Math.pow(x1[i][j] - x2[i][j], 2);
            }
        return dif;
    }
    public static Vector <int[][]> associate(Vector<int[][]> data,Vector<int[][]> avg, int tar) {
        Vector <int[][]>tmp=new <int[][]> Vector();
        int j;
        for(int i=0;i<data.size();i++){
            int dif=diffrence(data.get(i), avg.get(tar));
            for (j=0;j<avg.size();j++){
                int tempDiff=diffrence(data.get(i),avg.get(j));
                if(dif>tempDiff||(dif==tempDiff&&j<tar)) {
                    break;
                }
            }
            if(j==avg.size()){
                tmp.add(data.get(i));
            }
        }
        return tmp;
    }
    public static int[][] split(int[][] data,int value){
        int[][] temp=new int [data[0].length][data.length];
        for(int i=0;i<data[0].length;i++)
            for (int j=0;j<data.length;j++)
                temp[i][j]=data[i][j]+value;
        return temp;
    }
    public static int nearest_vector(Vector<int[][]> data,Vector<int[][]> avg, int tar){
        int min=diffrence(data.get(tar), avg.get(0));
        int nearest=0;
        for(int i=0;i<avg.size();i++){
            int diff=diffrence(data.get(tar), avg.get(i));
            if(diff<min){
                min=diff;
                nearest=i;
            }
        }
        return nearest;
    }
    public static void compress(RandomAccessFile original, RandomAccessFile compressed) throws IOException {
        original.seek(0);
        int levels=original.readInt();
        int w=original.readInt();
        int l=original.readInt();
        String path="";
        while (true){
            if(original.getFilePointer()>=original.length())
                break;
            char tmp;
            tmp=original.readChar();
            path+=tmp;
        }
        int[][] image=readGreyImage(path);
        Vector<int[][]> imageDividers=divideImage(image,w,l);
        Vector <int[][]> average=new Vector();
        int[][] imageDividersAverage=avg(imageDividers,w,l);
        average.add(split(imageDividersAverage,-1));
        average.add(split(imageDividersAverage,1));
        for(int i=0;i<levels-1;i++){
            Vector <int[][]> temp=new Vector();
            for(int j=0;j<average.size();j++){
                int[][] he5a=avg((associate(imageDividers,average,j)),w,l);
                temp.add(split((he5a),-1));
                temp.add(split((he5a),1));
            }
            average=temp;
        }
        int j=0;
        while (true){
            Vector average2=new Vector();
            for(int i=0;i<average.size();i++){
                average2.add(avg(associate(imageDividers,average,i),w,l));
            }
            if(average.equals(average2))
                break;
            j++;
            if(j==10)
                break;
            average=average2;
        }
        compressed.seek(0);
        compressed.writeInt(w);
        compressed.writeInt(l);
        compressed.writeInt(average.size());
        for(int i=0;i<average.size();i++)
            for(int q=0;q<w;q++)
                for (int p=0;p<l;p++)
                    compressed.writeInt(average.get(i)[q][p]);
        compressed.writeInt(image[0].length);
        compressed.writeInt(image.length);
        for (int i=0;i<imageDividers.size();i++){
            compressed.writeInt(nearest_vector(imageDividers,average,i));
        }
    }
    public static void decompress(String path,RandomAccessFile compressed) throws IOException {
        compressed.seek(0);
        int w=compressed.readInt();
        int l=compressed.readInt();
        int size=compressed.readInt();
        Vector <int[][]> codebook=new Vector();

        for(int i=0;i<size;i++) {
            int[][] temp=new int[w][l];
            codebook.add(temp);
            for (int q = 0; q < w; q++) {
                for (int p = 0; p < l; p++)
                    codebook.get(i)[q][p]=compressed.readInt();
            }
        }
        int width=compressed.readInt();
        int length=compressed.readInt();
        int[][] data=new int[width/w][length/l];
        for(int i=0;i<width/w;i++)
            for (int j=0;j<length/l;j++)
                data[i][j]=compressed.readInt();
        Vector <int [][]>compressedDataVector=new Vector();

        for (int i=0;i<width/w;i++){
            for(int j=0;j<length/l;j++)
                compressedDataVector.add(codebook.get(data[i][j]));
        }
        int [][]compressedData=new int[width][length];
        int wb=0;
        int lb=0;
        for(int i=0;i<compressedDataVector.size();i++){
            for(int j=0;j<w;j++) {
                for (int p = 0; p < l; p++){
                    compressedData[j+lb][p+wb]=compressedDataVector.get(i)[j][p];
                }
            }
            if((i+1)%(width/w)==0&&i!=0)
            {
                lb+=l;
                wb=0;
            }
            else
                wb+=w;
        }
        writeImage(compressedData,path);
    }
    public app(){
        compressbtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                RandomAccessFile original,compressed;
                try {
                    original=new RandomAccessFile(new File("original.txt"),"rw");
                    compressed=new RandomAccessFile(new File("compress.txt"),"rw");
                    original.setLength(0);
                    compressed.setLength(0);
                    original.seek(0);
                    int width= Integer.parseInt(width_txt.getText());
                    int height= Integer.parseInt(height_txt.getText());
                    int levels= Integer.parseInt(levels_txt.getText());
                    original.writeInt(levels);
                    original.writeInt(width);
                    original.writeInt(height);
                    original.writeChars(path.getText().toString());
                    compress(original,compressed);
                    String input =(path.getText().toString());
                    original.close();
                    JOptionPane.showMessageDialog(null,"compressed");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }});
        decompressbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                RandomAccessFile original,compressed;
                try {
                    compressed=new RandomAccessFile(new File("compress.txt"),"rw");
                    decompress("C:\\Users\\Hosam\\IdeaProjects\\image_quantization\\compressed.jpg",compressed);
                    JOptionPane.showMessageDialog(null,"decompressed");

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }});
    }
    public static void main(String[] args) {
        JFrame frame=new JFrame("app");
        frame.setContentPane(new app().jPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
