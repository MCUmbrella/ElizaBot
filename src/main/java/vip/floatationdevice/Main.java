package vip.floatationdevice;

import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        Eliza e = new Eliza();
        System.out.println(Eliza.WELCOME_MSG);
        Scanner sc = new Scanner(System.in);
        String s;
        for(; ; )
        {
            s = sc.nextLine();
            //System.err.println('=' + strNormalize(s));
            System.out.println(e.getResponse(s));
        }
    }
}
