package org.example.tools;

import org.example.annotation.Tool;
import org.example.annotation.ToolMethod;
import java.util.Map;

/**
 * Created on 2025/07/01
 */
@Tool(
        command = "shard",
        name = "分片计算(哈希取余)",
        description = "根据订单号获取hashcode，分片数取余，计算分片位置",
        parameters = {"orderId:订单号", "shards:分片数量(可选,默认10)"}
)
public class ShardingTool {

    private static final int DEFAULT_SHARD_COUNT = 10;

    @ToolMethod
    public String execute(Map<String, String> parameters) {
        String orderId = parameters.get("orderId");
        if (orderId == null || orderId.isEmpty()) {
            return "错误: 必须提供订单号";
        }

        int shards = DEFAULT_SHARD_COUNT;
        if (parameters.containsKey("shards")) {
            try {
                shards = Integer.parseInt(parameters.get("shards"));
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }

        int shard = calculateShard(orderId, shards);
        return "订单 '" + orderId + "' 应分到片: " + shard;
    }

    private int calculateShard(String orderId, int shardCount) {
        return Math.abs(orderId.hashCode()) % shardCount;
    }
}
