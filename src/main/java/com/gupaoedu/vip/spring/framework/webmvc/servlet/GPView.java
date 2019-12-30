package com.gupaoedu.vip.spring.framework.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Map;

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

    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
        StringBuffer sb = new StringBuffer();

        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");
    }
}
