package cn.notsick;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 算法工具类
 * Li (2019/2/24 13:28)
 */
public class AlgorithmUtil {

    private static final String SPEC_CHAR = "~!@#$%^&*()-_=+[]{};':,.<>/?";
    private static final String LETTER_DIGIT = "abcdefghijklmnopqrshtuvwxyzABXCDEFGHIJKLMNOPQRSTUVWXYZH0123456789";
    private static final String LETTER_DIGIT_PATTERN = "^(?![a-z0-9]+$)(?![A-Z0-9]+$)(?![a-zA-Z]+$)[a-zA-Z0-9]{6,28}";
    private static final String SPEC_PATTERN = "^(?![A-Za-z0-9]+$)(?![a-z0-9\\W]+$)(?![A-Za-z\\W]+$)(?![A-Z0-9\\W]+$)[a-zA-Z0-9\\W]{6,28}$";
    private static final Integer MIN_FIGURE = 6;
    private static final Integer MAX_FIGURE = 28;
    private static final String ONLY_DIGIT = "012";
    private static final String WITH_SPEC_CHAR = "aA0#";

    /**
     * 根据计算结果
     * masterPwd  主密码
     * key  关键词
     * figure  结果位数（6 <= figure <= 28）
     * type  密码类型："012"指仅数字；"aA0#" 指数字字母特殊符号；"aA0"指数字字母
     */
    static String compute(String masterPwd, String key, int figure, String type) {
        if (null == masterPwd || "".equals(masterPwd) || null == key || "".equals(key))
            return "主密码和关键词不能为空";

        if (figure < MIN_FIGURE || figure > MAX_FIGURE)
            return "位数范围需要在 6-28 位之间（包含 6 位和 28 位）";

        String[] baseTextArr = buildBaseText(masterPwd, key);

        if (type.equals(WITH_SPEC_CHAR)) {
            return buildSpecCharRet(baseTextArr, figure);
        } else if (type.equals(ONLY_DIGIT)) {
            return buildOnlyDigitRet(baseTextArr, figure);
        }

        String result = buildFinalRet(baseTextArr, figure);
        if (!result.matches(LETTER_DIGIT_PATTERN))
            return compute(result, result.substring(result.length()/2), figure, type);

        return result;
    }

    /**
     * 构建最终返回结果
     */
    private static String buildFinalRet(String[] middleTextArr, int figure) {
        StringBuilder tempSb = new StringBuilder();
        for (String s : middleTextArr) {
            tempSb.append(s);
        }

        return tempSb.substring(0, figure);
    }

    /**
     * 构建仅包含数字的字符数组
     */
    private static String buildOnlyDigitRet(String[] baseTextArr, int figure) {
        String[] middleArr = new String[MAX_FIGURE];
        System.arraycopy(baseTextArr, 0, middleArr, 0, MAX_FIGURE);
        int size = MAX_FIGURE - 1;
        for (int i=0; i<=size; i++) {
            int index = 0;
            for (int j=1; j<=9; j++) {
                if (middleArr[i].compareTo(middleArr[size-j]) < 0)
                    index++;
            }
            middleArr[i] = String.valueOf(LETTER_DIGIT.charAt(55 + index));
        }

        return buildFinalRet(middleArr, figure);
    }

    /**
     * 构建包含特殊字符的字符数组
     */
    private static String buildSpecCharRet(String[] baseTextArr, int figure) {
        String[] middleArr = new String[MAX_FIGURE];
        System.arraycopy(baseTextArr, 0, middleArr, 0, MAX_FIGURE);
        int size = MAX_FIGURE - 1;
        for (int i=0; i<= size; i++) {
            int index = 0;
            for (int j=0; j<=SPEC_CHAR.length()-1; j++) {
                if (middleArr[i].compareTo(middleArr[j]) < 0)
                    index ++;
            }
            if (Character.isLetter(middleArr[i].charAt(0)) && middleArr[i].compareTo(middleArr[size]) > 0)
                middleArr[i] = String.valueOf(SPEC_CHAR.charAt(index));
        }

        String result = buildFinalRet(middleArr, figure);
        if (!result.matches(SPEC_PATTERN))
            return compute(result, result.substring(result.length()/2), figure, WITH_SPEC_CHAR);

        return result;
    }

    /**
     * 根据主密码和关键词构建基础文本
     */
    private static String[] buildBaseText(String masterPwd, String key) {
        String appendMsgDigest = computeHexHmacMd5(masterPwd + key, key);
        String msgDigest = computeHexHmacMd5(masterPwd, key) + appendMsgDigest;
        String ruleDigest = computeHexHmacMd5(key, masterPwd) + appendMsgDigest;
        String[] result = new String[msgDigest.length()];
        int size = msgDigest.length() - 1;
        for (int i=0; i<=size; i++) {
            int index = 0;
            for (int j=0; j<=size; j++) {
                if (msgDigest.charAt(i) < ruleDigest.charAt(j))
                    index++;
            }
            result[i] = String.valueOf(LETTER_DIGIT.charAt(index));
        }

        return result;
    }

    /**
     * 计算 HMAC_MD5 消息摘要(一种键控哈希算法)
     */
    private static String computeHexHmacMd5(String data, String key) {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "HmacMD5");
        try {
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            return byteArr2HexStr(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 计算 MD5 消息摘要
     */
    private static String computeHexMd5(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data.getBytes(Charset.forName("UTF-8")));
            return byteArr2HexStr(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String byteArr2HexStr(byte[] bytes) {
        StringBuilder resultSb = new StringBuilder(bytes.length);
        for (Byte b : bytes) {
            String temp = Integer.toHexString(0xFF & b);
            if (temp.length() <2)
                resultSb.append(0);
            resultSb.append(temp);
        }

        return resultSb.toString();
    }

}
