package codebind;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;
import javax.imageio.ImageIO;
public  class image_quantization {
    public static int[][] avg(Vector<int[][]> data,int w,int l){
        int[][] r=new int[w][l];
        for(int j=0;j<w;j++)
            for(int k=0;k<l;k++)
                for(int i=0;i<data.size();i++){
                    r[j][k] += (int)(data.get(i)[j][k]);
                }

        for(int j=0;j<w;j++)
            for(int k=0;k<l;k++)
                if(data.size()!=0)
                    r[j][k]=r[j][k]/data.size();
        return r;
    }
    public static Vector<int[][]>  divideVector(int data[][],int w,int l){
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
    public static int[][] readImage(String path){
        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));
            int hieght=img.getHeight();
            int width=img.getWidth();
            int[][] imagePixels=new int[width][hieght];
            for(int x=0;x<width;x++){
                for(int y=0;y<hieght;y++){
                    int pixel=img.getRGB(x,y);
                    int red=(pixel  & 0x00ff0000) >> 16;
                    int grean=(pixel  & 0x0000ff00) >> 8;
                    int blue=pixel  & 0x000000ff;
                    int alpha=(pixel & 0xff000000) >> 24;
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
        int[][] temp=new int[x1[0].length][x1.length];
        for(int i=0;i<x1[0].length;i++)
             for(int j=0;j<x1.length;j++)
                    temp[i][j]=(x1[i][j]-x2[i][j])*(x1[i][j]-x2[i][j]);
        for(int i=0;i<temp[0].length;i++)
            for(int j=0;j<temp.length;j++)
                dif+=(temp[i][j]);
        return dif;
    }
    public static boolean equal(int tar,Vector<Integer> equals){
        for(int i=0;i<equals.size();i++){
            if(equals.get(i)<tar){
                return false;
            }
        }
        return true;
    }
    public static Vector <int[][]> associate(Vector<int[][]> data,Vector<int[][]> avg, int tar) {
        Vector <int[][]>tmp=new <int[][]> Vector();
        int j=0;
        for(int i=0;i<data.size();i++){
            int dif=diffrence(data.get(i), avg.get(tar));
            Vector equals=new Vector();
            for (j=0;j<avg.size();j++){
                if(dif>diffrence(data.get(i),avg.get(j)))
                {
                    break;
                }
                else if(dif==diffrence(data.get(i),avg.get(j))&&j!=tar){
                    equals.add(j);
                }
            }
            if(j==avg.size()&&equal(tar,equals)){
                tmp.add(data.get(i));
            }
        }
        return tmp;
    }
    public static int[][] split(int[][] data,int value){
        int[][] temp=new int [data[0].length][data.length];
        if(value==1)
        {
            for(int i=0;i<data[0].length;i++)
                for (int j=0;j<data.length;j++)
                    temp[i][j]=(int)data[i][j]+1;
        }
        if(value==-1)
        {
            for(int i=0;i<data[0].length;i++)
                for (int j=0;j<data.length;j++)
                    temp[i][j]=(int)data[i][j]-1;
        }
        return temp;

    }
    public static int nearest_vector(Vector<int[][]> data,Vector<int[][]> avg, int tar){
        int min=diffrence(data.get(tar), avg.get(0));
        int nearest=0;
        for(int i=0;i<avg.size();i++){
                if(diffrence(data.get(tar), avg.get(i))<min){
                    min=diffrence(data.get(tar), avg.get(i));
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
        int[][] image=readImage(path);
        //int image[][]=readImage(path);
        Vector<int[][]> image_div=divideVector(image,w,l);
        Vector range=new Vector();
        Vector data=new Vector();
        /*for(int i=0;i<data_r.size();i++){
            data.add(data_r.get(i));
        }*/
        Vector <int[][]> average=new Vector();
        average.add(split((avg(image_div,w,l)),-1));
        average.add(split((avg(image_div,w,l)),1));
        for(int i=0;i<levels-1;i++){
            Vector <int[][]> temp=new Vector();
            for(int j=0;j<average.size();j++){
                temp.add(split((avg((associate(image_div,average,j)),w,l)),-1));
                temp.add(split((avg(associate(image_div,average,j),w,l)),1));
            }
            average=temp;
        }
        int j=0;
        while (true){
            boolean x=true;
            Vector average2=new Vector();
            for(int i=0;i<average.size();i++){
                average2.add(avg(associate(image_div,average,i),w,l));
            }
            for(int i=0;i<average.size();i++){
                if((int[][])average.get(i)!= (int[][]) average2.get(i))
                {
                    x=false;
                    break;
                }
            }
            j++;
            if(x==true)
                break;
            if(j==5)
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
        for (int i=0;i<image_div.size();i++){
            compressed.writeInt(nearest_vector(image_div,average,i));
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




    public static void main(String[] args) throws IOException {
        int[][] pixels=readImage("D:\\FCI\\\\baby.jpg");
        RandomAccessFile original,compressed;
        original=new RandomAccessFile(new File("original.txt"),"rw");
        compressed=new RandomAccessFile(new File("com.txt"),"rw");

        /*int[][] pixels=new int[6][6];
        pixels[0]= new int[]{1, 2, 7, 9, 4, 11};
        pixels[1]= new int[]{3, 4, 6, 6, 12, 12};
        pixels[2]= new int[]{4, 9, 15, 14, 9, 9};
        pixels[3]= new int[]{10, 10, 20, 18, 8, 8};
        pixels[4]= new int[]{4, 3, 17, 16, 1, 4};
        pixels[5]= new int[]{4, 5, 18, 18, 5, 6};*/
        original.setLength(0);
        compressed.setLength(0);
        compress(original,compressed);
        decompress("D:\\FCI\\uuu.jpg",compressed);
        /*Vector<int[][]> r=divideVector(pixels,2,2);
        int[][] qqq=(avg(r,2,2));
        int[][] x1 = new int[2][2];
        x1[0][0]=(int)qqq[0][0];
        x1[0][1]=(int)qqq[0][1];
        x1[1][0]=(int)qqq[1][0];
        x1[1][1]=(int)qqq[1][1];
        int[][] x2 = new int[2][2];
        x2[0][0]=(int)qqq[0][0]+1;
        x2[0][1]=(int)qqq[0][1]+1;
        x2[1][0]=(int)qqq[1][0]+1;
        x2[1][1]=(int)qqq[1][1]+1;
        Vector<int[][]> data=new Vector<>();
        data.add(x1);
        data.add(x2);
        Vector<int[][]> a=associate(r,data,1);
        System.out.println(a.size());
        for(int k=0;k<a.size();k++) {
            for (int i = 0; i < a.get(0)[0].length; i++) {
                for (int j = 0; j < a.get(0).length; j++) {
                    System.out.print(a.get(k)[i][j] + " ");
                }
                System.out.println("");
            }
            System.out.println('X');
        }
*/
    }
}