package com.qg.airubbish.utils;

import okhttp3.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static com.qg.airubbish.constant.Constant.*;

/**
 * @author jason.zhao
 */
public class HttpUtils {
    public static String post(String requestUrl, String accessToken, String params)
            throws Exception {
        String contentType = "application/x-www-form-urlencoded";
        return HttpUtils.post(requestUrl, accessToken, contentType, params);
    }

    public static String post(String requestUrl, String accessToken, String contentType, String params)
            throws Exception {
        String encoding = "UTF-8";
        if (requestUrl.contains("nlp")) {
            encoding = "GBK";
        }
        return HttpUtils.post(requestUrl, accessToken, contentType, params, encoding);
    }

    public static String post(String requestUrl, String accessToken, String contentType, String params, String encoding)
            throws Exception {
        String url = requestUrl + "?access_token=" + accessToken;
        return HttpUtils.postGeneralUrl(url, contentType, params, encoding);
    }

    public static String postGeneralUrl(String generalUrl, String contentType, String params, String encoding)
            throws Exception {
        URL url = new URL(generalUrl);
        // 打开和URL之间的连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // 设置通用的请求属性
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // 得到请求的输出流对象
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(params.getBytes(encoding));
        out.flush();
        out.close();

        // 建立实际的连接
        connection.connect();
        // 获取所有响应头字段
        Map<String, List<String>> headers = connection.getHeaderFields();
        // 遍历所有的响应头字段
        for (String key : headers.keySet()) {
            System.err.println(key + "--->" + headers.get(key));
        }
        // 定义 BufferedReader输入流来读取URL的响应
        BufferedReader in = null;
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), encoding));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        System.err.println("result:" + result);
        return result;
    }

    public static String getSync(String url){//get 同步请求
        String s="";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();  //execute() : 同步, enqueue() : 异步
            s = response.body().string();  //获取数据
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String postSync(String url, String body) {  //post同步请求;body为参数列表
        String res = "";
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType=MediaType.Companion.parse("application/x-www-form-urlencoded");
        RequestBody stringBody=RequestBody.Companion.create(body,mediaType);
        Request request=new Request.Builder().url(url).post(stringBody).build();

        try {
            Response response = client.newCall(request).execute();
            res = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void getAsync(String url, Callback callback) {//get异步
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    public static void postAsync(String url,String body,Callback callback) {//post异步
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType=MediaType.Companion.parse("application/json;charset=utf-8");
        RequestBody stringBody=RequestBody.Companion.create(body,mediaType);
        Request request=new Request.Builder().url(url).post(stringBody).build();

        client.newCall(request).enqueue(callback);
    }


    /**
     * 获得内网IP
     * @return 内网IP
     */
    public static String getIntranetIp (){
        try{
            return InetAddress.getLocalHost().getHostAddress();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得外网IP
     * @return 外网IP
     */
    public static String getExtranetIp(){
        try{
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            Enumeration<InetAddress> addrs;
            while (networks.hasMoreElements())
            {
                addrs = networks.nextElement().getInetAddresses();
                while (addrs.hasMoreElements())
                {
                    ip = addrs.nextElement();
                    if (ip != null
                            && ip instanceof Inet4Address
                            && ip.isSiteLocalAddress()
                            && !ip.getHostAddress().equals(getIntranetIp()))
                    {
                        return ip.getHostAddress();
                    }
                }
            }

            // 如果没有外网IP，就返回内网IP
            return getIntranetIp();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String getDomainName(){
        return DNS;
    }

    public static String getServerUrlOfHTTP(){
        if(getExtranetIp().contains(LOCALHOST_IP)){
            return SERVER_PROTOCOL_1+LOCALHOST_IP+SERVER_PORT;
        }
        return SERVER_PROTOCOL_1+DNS+SERVER_PORT;
    }

    public static String getServerUrlOfHTTPS(){
        if(getExtranetIp().contains(LOCALHOST_IP)){
            return SERVER_PROTOCOL_2+LOCALHOST_IP+SERVER_PORT;
        }
        return SERVER_PROTOCOL_2+DNS+SERVER_PORT;
    }

    /***
     * Compatible with GET and POST
     *
     * @param request
     * @return : <code>byte[]</code>
     * @throws IOException
     */
    public static byte[] getRequestQuery(HttpServletRequest request)
            throws IOException {
        String submitMehtod = request.getMethod();
        String queryString = null;

        // GET
        if (submitMehtod.equals("GET")) {
            queryString = request.getQueryString();
            // charset
            String charEncoding = request.getCharacterEncoding();
            if (charEncoding == null) {
                charEncoding = "UTF-8";
            }
            return queryString.getBytes(charEncoding);
        } else {// POST
            return getRequestPostBytes(request);
        }
    }

    /***
     * Get request query string, form method : post
     *
     * @param request
     * @return byte[]
     * @throws IOException
     */
    public static byte[] getRequestPostBytes(HttpServletRequest request)
            throws IOException {
        int contentLength = request.getContentLength();
        if(contentLength<0){
            return null;
        }
        byte buffer[] = new byte[contentLength];
        for (int i = 0; i < contentLength;) {

            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }
    /***
     * Get request query string, form method : post
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static String getRequestPostStr(HttpServletRequest request)
            throws IOException {
        byte buffer[] = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        if(buffer==null){
            return new String("{}");
        }else{
            return new String(buffer, charEncoding);
        }

    }

    /**
     * 拦截获取请求头部分
     * @param request
     */
    public static void getRequestHeader(HttpServletRequest request){
        Enumeration<String> headerNames = request.getHeaderNames();

        //判断是否还有下一个元素
        while(headerNames.hasMoreElements()) {
            //获取headerNames集合中的请求头
            String nextElement = headerNames.nextElement();
            //通过请求头得到请求内容
            String header = request.getHeader(nextElement);
            System.out.println(nextElement + " : " + header);
        }


    }

    public static String getRemoteIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getRemoteAddr();
        }
        return ip;
    }


    public static String getRemoteInformation(HttpServletRequest request) {
        //客户端Ip地址
        String remoteIp = HttpUtils.getRemoteIpAddress(request);
        //客户端Port
        int port =request.getRemotePort();
        //客户端请求Url
        String url = request.getRequestURL().toString();

        String uri = request.getRequestURI();

        String information = "IP："+remoteIp+", "+"Port:"+port+", "+"发起的请求URL:"+url+", "+"请求URI:"+uri;
        return information;
    }


}
