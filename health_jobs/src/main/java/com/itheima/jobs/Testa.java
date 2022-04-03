package com.itheima.jobs;

import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @version V1.0
 * @Author liyuan
 * @Description //TODO $
 * @Date $ $
 * @Param $
 * @return $
 **/
public class Testa {
    /*@Author liyuan
     *@Description aabbbccccdddddeeeeeeeeefff234tttdddfffbbbggg   去除相邻元素的重复元素。
     *@Date 11:13 2021/4/10
     *@Param
     *@return
    **/
    public String removeSubString(String s){
        StringBuilder sb = new StringBuilder();
        int i=0;
        char c = 0;
        while (i<s.length()){
            c = s.charAt(i);
            sb.append(c);
            i++;
            while (i<s.length()&&c==s.charAt(i)){
                i++;
            }
        }

        return sb.toString();
    }

    /*
/*请编写一个函数来查找字符串数组中的最长公共前缀。
如果不存在公共前缀，返回空字符串 ""。
例子：
输入：strs = ["chien","chat","cheval"]
输出："ch"

输入：strs = ["dog","cat","horse"]
输出：""
解释：输入不存在公共前缀。

说明：
strs[i] 仅由小写英文字母组成 */
    public String longestCommonPrefix(String[] strs) {
        if(strs==null || strs.length==0) return "字符不存在";

        for (int i=0;i<strs[0].length();i++){
            char c= strs[0].charAt(i);
            for (int j=1;j<strs.length;j++){
               if(strs[j].length()<=i || strs[j].charAt(i)!=c){//前缀等于数组元素。
                   return (strs[0].substring(0,i));
               }
            }
            //System.out.println(c);
        }

        return "";
    }

    public String te(String s){
        StringBuilder sb = new StringBuilder();
        int i=0;
        while (i<s.length()){
            char c = s.charAt(i);
            sb.append(c);
            i++;
            while (i<s.length()&&c==s.charAt(i)){
                i++;
            }
        }

        return sb.toString();
    }

    public static String longestCommonPrefix1(String[] strs) {
        int count = strs.length;
        String prefix = "";
        if(count != 0){
            prefix = strs[0];
        }
        for(int i=0; i<count; i++){
            //关键代码，不断的从后往前截取字符串，然后与之相比，直到startsWith()返回true
            while(!strs[i].startsWith(prefix)){
                prefix = prefix.substring(0, prefix.length()-1);
            }
        }
        return prefix;
    }

    @Test
    public void Test(){
        String []str1 = {"chien","ch","cheval"};
        String []str2 = {"dog","cat","horse"};
        System.out.println(longestCommonPrefix1(str1));
    }

    @Test
    public void ceshi(){
        //System.out.println(removeRepeatChar("aabbbccccdddddeeeeeeeeefff234tttdddfffbbbggg"));
        System.out.println(te("aabbbccccdddddeeeeeeeeefff234tttdddfffbbbggg"));
String s= "aabbbccccdddddeeeeeeeeefff234tttdddfffbbbggg";
        StringBuffer sb = new StringBuffer(s);
        for (int i=0;i<sb.length()-1;i++){
            for (int j=i+1;j<sb.length();j++){
                if (s.charAt(i)==s.charAt(j)){
                    sb.deleteCharAt(j);
                }
            }
        }

        System.out.println(sb.toString());
    }

    public String removeRepeatChar(String s){
        if (s==null){
            return "空";
        }
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int len = s.length();
        while (i < len) {
            char c = s.charAt(i);
            sb.append(c);
            i++;
            while (i < len && s.charAt(i) == c) {//这个是如果这两个值相等，就让i+1取下一个元素,一直取到不相等的时候才将当前i；append
                i++;
            }
        }
        return sb.toString();
    }
}