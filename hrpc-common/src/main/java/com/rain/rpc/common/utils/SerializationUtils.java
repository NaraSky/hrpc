package com.rain.rpc.common.utils;

import java.util.stream.IntStream;

/**
 * 序列化工具类
 * 提供序列化类型字符串的填充和去除填充操作
 * 保证序列化类型字段在协议中固定长度，便于协议解析
 *
 * @author rain
 * @since 1.0.0
 */
public class SerializationUtils {

    /**
     * 填充字符，用于补足序列化类型字符串到固定长度
     */
    private static final String PADDING_STRING = "0";

    /**
     * 约定序列化类型最大长度为16
     * 保证不同序列化类型在协议中占用相同字节数，简化协议解析
     */
    public static final int MAX_SERIALIZATION_TYPE_COUNT = 16;

    /**
     * 为长度不足16的字符串后面补0，使其达到固定长度
     * 保证序列化类型字段在协议中始终占用16字节，便于协议解析
     *
     * @param str 原始字符串
     * @return 补0后的固定长度字符串
     */
    public static String paddingString(String str) {
        str = transNullToEmpty(str);
        // 如果字符串长度已达到或超过最大长度，则直接返回原字符串
        if (str.length() >= MAX_SERIALIZATION_TYPE_COUNT) {
            return str;
        }
        
        // 计算需要填充的字符数量
        int paddingCount = MAX_SERIALIZATION_TYPE_COUNT - str.length();
        
        // 使用StringBuilder构建填充后的字符串，提高性能
        StringBuilder paddingStringBuilder = new StringBuilder(str);
        
        // 在字符串末尾添加指定数量的填充字符
        IntStream.range(0, paddingCount).forEach((i) -> {
            paddingStringBuilder.append(PADDING_STRING);
        });
        
        return paddingStringBuilder.toString();
    }

    /**
     * 去除字符串末尾的填充字符"0"
     * 还原原始的序列化类型字符串
     *
     * @param str 带填充字符的字符串
     * @return 去除填充字符后的原始字符串
     */
    public static String subString(String str) {
        str = transNullToEmpty(str);
        // 去除字符串中的所有填充字符"0"
        return str.replace(PADDING_STRING, "");
    }

    /**
     * 将null转换为空字符串
     * 防止字符串操作时出现空指针异常
     *
     * @param str 待转换的字符串
     * @return 转换后的字符串，如果原字符串为null则返回空字符串
     */
    public static String transNullToEmpty(String str) {
        return str == null ? "" : str;
    }
}