package org.example.tools;

import org.example.annotation.Tool;
import org.example.annotation.ToolMethod;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Created on 2025/08/26
 */
@Tool(
        command = "timestamp",
        name = "时间戳日期互转",
        description = "时间戳日期互转",
        parameters = {"date:日期(yyyy-MM-dd HH:mm:ss)默认当前时间", "timestamp:毫秒时间戳"}
)
public class TimestampTool {

    // 定义日期格式
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ToolMethod
    public String execute(Map<String, String> parameters) {
        String date = parameters.get("date");
        String timestamp = parameters.get("timestamp");
        if (timestamp != null && !timestamp.isEmpty()) {
            return timestampToDate(timestamp);
        } else {
            return dateToTimestamp(date);
        }
    }


    /**
     * 将日期字符串转换为毫秒时间戳字符串
     * @param dateString 日期字符串，格式为"yyyy-MM-dd HH:mm:ss"
     * @return 毫秒时间戳字符串
     * @throws DateTimeParseException 如果日期格式不正确
     */
    public static String dateToTimestamp(String dateString) throws DateTimeParseException {
        LocalDateTime dateTime;
        if (dateString == null || dateString.isEmpty()) {
            dateTime = LocalDateTime.now();
        } else {
            // 解析日期字符串
            dateTime = LocalDateTime.parse(dateString, FORMATTER);
        }
        // 转换为毫秒时间戳
        long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // 返回字符串形式的毫秒时间戳
        return dateTime.format(FORMATTER) + "\n" + timestamp;
    }

    /**
     * 将毫秒时间戳字符串转换为日期字符串
     * @param timestampString 毫秒时间戳字符串
     * @return 日期字符串，格式为"yyyy-MM-dd HH:mm:ss"
     * @throws NumberFormatException 如果时间戳格式不正确
     */
    public static String timestampToDate(String timestampString) throws NumberFormatException {
        // 解析时间戳字符串为long类型
        long timestamp = Long.parseLong(timestampString);

        // 转换为LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        );

        // 格式化为日期字符串
        return dateTime.format(FORMATTER) + "\n" + timestamp;
    }
}
