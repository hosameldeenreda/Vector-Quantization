package codebind;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        File compressed;
        int levels,width,length;
        String imagepath,compressedPath;
        compressed=new File("com.txt");
        Scanner sc= new Scanner(System.in);
        System.out.print("Enter number of levels ");
        levels= sc.nextInt();
        System.out.print("Enter width of the vector ");
        width= sc.nextInt();
        System.out.print("Enter length of the vector ");
        length= sc.nextInt();
        System.out.print("Enter path of the photo ");
        imagepath=sc.next();
        System.out.print("Enter path of the compressed photo ");
        compressedPath=sc.next();
        LBG.compress(compressed,levels,width,length,imagepath);
        LBG.decompress(compressedPath,compressed);
    }

}
