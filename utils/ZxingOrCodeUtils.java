package com.qg.airubbish.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.qg.airubbish.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.zxing.client.j2se.MatrixToImageConfig.BLACK;
import static com.google.zxing.client.j2se.MatrixToImageConfig.WHITE;
/**
 * Created by lingsf on 2019/4/29.
 */
public class ZxingOrCodeUtils {
    private static final Logger logger = LoggerFactory.getLogger(ZxingOrCodeUtils.class);
    /**
     * 生成二维码图片
     * @param content 二维码中的数据
     * @param filePath 生成二维码的根路径   fileName文件名
     * @param deleteWhite 默认二维码边上是带有白边的，传true时会去掉白边
     */
    public static void testEncode(String content,String filePath,String fileName,boolean deleteWhite) throws WriterException, IOException {

        int width = 200; // 图像宽度
        int height = 200; // 图像高度
        String format = "png";// 图像类型
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵
        if(deleteWhite){ //删除白边
            BufferedImage image = deleteWhite(bitMatrix); //去白边的话加这一行
            File outputfile = new File(filePath+fileName);
            ImageIO.write(image, "png", outputfile);
            return;
        }

        Path path = FileSystems.getDefault().getPath(filePath, fileName);
        MatrixToImageWriter.writeToPath(bitMatrix, format, path);// 输出图像
        System.out.println("输出成功.");
    }

    /**
     * 生成二维码图片内容
     * @param filePath  文件的绝对路径 例如："D://zxing.png";
     */
    public static void testDecode(String filePath) {
        BufferedImage image;
        try {
            image = ImageIO.read(new File(filePath));
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);// 对图像进行解码
            System.out.println(result.getText());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    //去白边的话，调用这个方法
    private static BufferedImage deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        logger.info("去白边deleteWhite---rec："+rec);
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1])){
                    resMatrix.set(i, j);
                }
            }
        }

        int width = resMatrix.getWidth();
        int height = resMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, resMatrix.get(x, y) ? BLACK
                        : WHITE);
            }
        }
        return image;
    }
    public static void main(String[] args){
        //1、去白边的方法例子
//        String fileName = "zxing.png";
//        String content = "西秀区农村土地承包经营权证\n" +
//                "权证编码：398881111222211J\n" +
//                "发包方名称：轿子山镇大进村民委员会\n" +
//                "承包方代表：杨井岗\n" +
//                "确权总面积：12.33亩\n" +
//                "地块总数:13块";//内容信息
//        try {
//            testEncode(content,"D://",fileName,true);
//        } catch (WriterException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //2、简单的生成二维码例子
        testDecode("D://zxing.png");
    }
}


