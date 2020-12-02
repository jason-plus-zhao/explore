package com.qg.airubbish.utils;

import com.aliyun.oss.OSSClient;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.qg.airubbish.constant.AliyunConstants;
import com.qg.airubbish.vo.FileUploadVO;
import org.apache.commons.lang.StringUtils;

import java.io.*;


/**
 * @author: jason.zhao
 * @createTime: 2020/9/22 10:27
 * @version: v1.0
 * @description:
 */
public class StsServiceSample {
    public static void main(String[] args) throws FileNotFoundException {
        String endpoint = "sts.cn-hangzhou.aliyuncs.com";
        String accessKeyId = "xxxx";
        String accessKeySecret = "xxxx";
        String roleArn = "acs:ram::1968161943854761:role/ramosstest";
        String roleSessionName = "RamOssTest";


        String AccessKeyId="";
        String AccessKeySecret="";
        String SecurityToken="";
        String Endpoint="";

        String policy = null;
/*                "{\n" +
                "    \"Version\": \"1\", \n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Action\": [\n" +
                "                \"oss:*\"\n" +
                "            ], \n" +
                "            \"Resource\": [\n" +
                "                \"acs:oss:*:*:*\" \n" +
                "            ], \n" +
                "            \"Effect\": \"Allow\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";*/
        try {
            //构造default profile（参数留空，无需添加Region ID）
            IClientProfile profile = DefaultProfile.getProfile("", accessKeyId, accessKeySecret);
            //用profile构造client
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysEndpoint(endpoint);
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(policy); // Optional
            final AssumeRoleResponse response = client.getAcsResponse(request);
            System.out.println("Expiration: " + response.getCredentials().getExpiration());
            System.out.println("Access Key Id: " + response.getCredentials().getAccessKeyId());
            System.out.println("Access Key Secret: " + response.getCredentials().getAccessKeySecret());
            System.out.println("Security Token: " + response.getCredentials().getSecurityToken());
            System.out.println("RequestId: " + response.getRequestId());

            AccessKeyId=response.getCredentials().getAccessKeyId();
            AccessKeySecret=response.getCredentials().getAccessKeySecret();
            SecurityToken=response.getCredentials().getSecurityToken();
            Endpoint="http://oss-accelerate.aliyuncs.com";



        } catch (ClientException e) {
            System.out.println("Failed：");
            System.out.println("Error code: " + e.getErrCode());
            System.out.println("Error message: " + e.getErrMsg());
            System.out.println("RequestId: " + e.getRequestId());
        }

        // 用户拿到STS临时凭证后，通过其中的安全令牌（SecurityToken）和临时访问密钥（AccessKeyId和AccessKeySecret）生成OSSClient。
        // 创建OSSClient实例。注意，这里使用到了上一步生成的临时访问凭证（STS访问凭证）。
        OSSClient ossClient = new OSSClient(Endpoint, AccessKeyId, AccessKeySecret, SecurityToken);

        // OSS操作。。。

        //上传文件
        File newFile =new File("C:\\Users\\jason.zhao\\Desktop\\垃圾分类2.mp4");
        ossClient.putObject("media-upload-bucket","video_resource/垃圾分类2.mp4",newFile);

        //下载文件
/*        InputStream inputStream=ossClient.getObject("media-upload-bucket","user_avatar/jisoo.jpg").getObjectContent();
        File file = new File("C:\\Users\\jason.zhao\\Desktop\\user_avatar\\","jisoo.jpg");
        try {
            FileUtils.writeToLocal(file.getAbsolutePath(),inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // 关闭OSSClient。
        ossClient.shutdown();




    }
}
