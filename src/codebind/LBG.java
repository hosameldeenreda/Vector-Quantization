package codebind;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;

public class LBG {
    private static int[][] avg(Vector<int[][]> data, int w, int l){
        int[][] r=new int[w][l];
        if(data.size()==0)
            return r;
            for (int j = 0; j < w; j++) {
                for (int k = 0; k < l; k++) {
                    for (int i = 0; i < data.size(); i++) {
                        r[j][k] += data.get(i)[j][k];
                    }
                    r[j][k] /= data.size();
                }
            }
        return r;
    }
    private static Vector<int[][]> divideImage(int data[][], int w, int l){
        Vector<int[][]> imageDividers=new Vector<int[][]>();
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
                imageDividers.add(temp);
            }
            lb=0;
            wb+=w;
        }
        return imageDividers;
    }
    private static int[][] readGreyImage(String path){
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
            System.out.println("image not found");
            return null;
        }

    }
    private static void writeImage(int[][] imagePixels, String outPath){
        BufferedImage image = new BufferedImage(imagePixels.length, imagePixels[0].length, BufferedImage.TYPE_INT_RGB);
        for (int y= 0; y < imagePixels.length; y++) {
            for (int x = 0; x < imagePixels[y].length; x++) {
                int value= 0xff000000 | (imagePixels[y][x]<<16) | (imagePixels[y][x]<<8) | (imagePixels[y][x]);
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
    private static int diffrence(int x1[][], int x2[][]){
        int dif=0;
        for(int i=0;i<x1[0].length;i++)
            for(int j=0;j<x1.length;j++) {
                dif+=(int) Math.pow(x1[i][j] - x2[i][j], 2);
            }
        return dif;
    }
    private static Vector <int[][]> associate(Vector<int[][]> data,Vector<int[][]> avg, int tar) {
        Vector tmp=new <int[][]> Vector();
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
    private static void  split(Vector <int[][]> vector,int[][] data){
        int[][] temp=new int [data[0].length][data.length];
        int[][] temp2=new int [data[0].length][data.length];
        for(int i=0;i<data[0].length;i++)
            for (int j=0;j<data.length;j++) {
                temp[i][j] = data[i][j] + 1;
                temp2[i][j] = data[i][j] - -1;
            }
        vector.add(temp);
        vector.add(temp2);
    }
    private static int nearest_vector(Vector<int[][]> data,Vector<int[][]> avg, int tar){
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
    static void compress(File compressed,int levels,int w,int l,String path) throws IOException {
        int[][] image=readGreyImage(path);
        if(image==null)
            return;
        Vector<int[][]> imageDividers=divideImage(image,w,l);
        Vector <int[][]> average=new Vector();
        int[][] imageDividersAverage=avg(imageDividers,w,l);
        split(average,imageDividersAverage);
        for(int i=0;i<levels-1;i++){
            Vector <int[][]> temp=new Vector();
            for(int j=0;j<average.size();j++){
                int[][] he5a=avg((associate(imageDividers,average,j)),w,l);
                split(temp,he5a);
            }
            average=temp;
        }
        FileWriter write = new FileWriter(compressed.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(write);
        bw.write(w+" ");
        bw.write(l+" ");
        bw.write(average.size()+" ");
        for(int i=0;i<average.size();i++)
            for(int q=0;q<w;q++)
                for (int p=0;p<l;p++)
                    bw.write(average.get(i)[q][p]+" ");
        bw.write(image.length+" ");
        bw.write(image[0].length+" ");
        for (int i=0;i<imageDividers.size();i++){
            bw.write(nearest_vector(imageDividers,average,i)+" ");
        }
        bw.close();
    }
    static void decompress(String path, File compressed) throws IOException {
        Scanner sc = new Scanner(compressed);
        int w=Integer.valueOf(sc.next());
        int l=Integer.valueOf(sc.next());
        int codebookSize=Integer.valueOf(sc.next());
        Vector <int[][]> codebook=new Vector();
        for(int i=0;i<codebookSize;i++) {
            int[][] temp=new int[w][l];
            for (int q = 0; q < w; q++) {
                for (int p = 0; p < l; p++)
                    temp[q][p]=Integer.valueOf(sc.next());
            }
            codebook.add(temp);
        }
        int width=Integer.valueOf(sc.next());
        int length=Integer.valueOf(sc.next());
        Vector <int [][]>compressedDataVector=new Vector();
        for(int i=0;i<width/w;i++)
            for (int j=0;j<length/l;j++)
                compressedDataVector.add(codebook.get(Integer.valueOf(sc.next())));

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
}
