package com.example.controller;

import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class FileController {

    /** 统一根目录：项目根/files/ */
    private static final String FILE_ROOT = System.getProperty("user.dir") + File.separator + "files" + File.separator;
    @Value("${fileBaseUrl:http://localhost:9090}")
    private String fileBaseUrl;
    /** 通用上传（图片/视频/任意文件） */
    @PostMapping("/files/upload")
    public Result upload(MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件不能为空");
        String orig = file.getOriginalFilename();
        String suffix = orig.substring(orig.lastIndexOf('.'));
        String fileName = System.currentTimeMillis() + suffix;
        File dir = new File(FILE_ROOT);
        if (!dir.exists()) dir.mkdirs();
        try {
            FileUtil.writeBytes(file.getBytes(), FILE_ROOT + fileName);
            // 返回统一访问地址
            String url = fileBaseUrl + "/files/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            return Result.error("上传失败");
        }
    }

    /** 统一文件读取（图片/视频在线播放/显示） */
    @GetMapping("/files/{fileName}")
    public void getFile(@PathVariable String fileName, HttpServletResponse resp) throws IOException {
        String realPath = FILE_ROOT + fileName;
        if (!FileUtil.exist(realPath)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("File not found");
            return;
        }

        // 设置正确的 Content-Type
        String contentType = getContentType(fileName);
        resp.setContentType(contentType);
        resp.setHeader("Cache-Control", "max-age=3600"); // 缓存1小时

        byte[] bytes = FileUtil.readBytes(realPath);
        ServletOutputStream os = resp.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
    }

    /** 获取文件类型 */
    private String getContentType(String fileName) {
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (fileName.endsWith(".avi")) {
            return "video/x-msvideo";
        } else {
            return "application/octet-stream";
        }
    }

    /** 文件下载（可选保留） */
    @GetMapping("/files/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        String realPath = FILE_ROOT + fileName;
        ServletOutputStream os = resp.getOutputStream();
        os.write(FileUtil.readBytes(realPath));
        os.flush();
        os.close();
    }
}