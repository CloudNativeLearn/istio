package com.example.servicejava.controller;


import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/")
public class controller {


    @Value("${servise3Address}")
    private String name;

    @GetMapping("/")
    public Map getType(){
        System.out.println("查询员工");

        System.out.println(name);




        Map<String, Object> maps = new HashMap<>();
        maps.put("语言","java");
        maps.put("状态","服务二调用成功");
        maps.put("版本","v1");

        Map<String, Object> result = new HashMap<>();


        result.put("服务二",maps);
        if (this.httpURLGETCase().toString().equals("")){
            // 调用三失败
            Map<String, Object> erMap = new HashMap<>();
            erMap.put("状态","服务二调用失败");
            erMap.put("版本","未知");
            erMap.put("语言","未知");
            result.put("服务三",erMap);
        }else {
            Map stringToMap =  JSONObject.parseObject(this.httpURLGETCase().toString());
            result.put("服务三",stringToMap);
        }

        return result;

    }






    public StringBuilder httpURLGETCase() {
        String methodUrl = name;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String line = null;
        StringBuilder result = new StringBuilder();


        try {
            URL url = new URL(methodUrl );
            connection = (HttpURLConnection) url.openConnection();// 根据URL生成HttpURLConnection
            connection.setRequestMethod("GET");// 默认GET请求
            connection.connect();// 建立TCP连接
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));// 发送http请求
                // 循环读取流
                while ((line = reader.readLine()) != null) {
                    result.append(line).append(System.getProperty("line.separator"));// "\n"
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                connection.disconnect();
                return result;
            }

        }






    }


















}
