package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: dahai.li
 * @Description:
 * @Date: Create in 15:33 2019/12/30
 */
public class GPView {

    private File viewFile;

    public GPView(File templateFile) {
        this.viewFile = templateFile;
    }

    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //第一步，就是把模板文件内容读出来
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");
        //解析，把模板里面的模板语言替换掉
        String line = null;
        while (null != (line = ra.readLine())) {
            line = new String(line.getBytes("ISO-8859-1"), "utf-8");
            Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String paramName = matcher.group();
                paramName = paramName.replaceAll("￥\\{|\\}", "");
                Object paramVlue = model.get(paramName);
                if (null == paramVlue) {
                    continue;
                }
                line = matcher.replaceFirst(makeStringForRegExp(paramVlue.toString()));
                matcher = pattern.matcher(line);
            }
            sb.append(line);
        }
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(sb.toString());
    }


    //处理特殊字符
    public static String makeStringForRegExp(String str) {
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }
}
