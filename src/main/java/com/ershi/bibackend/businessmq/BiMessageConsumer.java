package com.ershi.bibackend.businessmq;

import com.ershi.bibackend.common.BiModelConstant;
import com.ershi.bibackend.common.BiMqConstant;
import com.ershi.bibackend.common.ErrorCode;
import com.ershi.bibackend.exception.BusinessException;
import com.ershi.bibackend.manager.AIManager;
import com.ershi.bibackend.model.entity.Chart;
import com.ershi.bibackend.model.enums.ChartStatusEnum;
import com.ershi.bibackend.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;


@Slf4j
@Component
public class BiMessageConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AIManager aiManager;


    // 监听的消息队列
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("接收消息 = {}", message);

        try {
            // 消息参数检验
            if (StringUtils.isBlank(message)) {
                /*
                 * basicNack(long deliveryTag, boolean multiple, boolean requeue)
                 */
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "");
            }

            // 根据 chartId 查询需要执行的任务数据
            // message => chartId
            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            if (chart == null) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务图表为空");
            }

            // 修改任务状态为 RUNNING
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(ChartStatusEnum.RUNNING.getValue());
            updateChart.setExecuteMessage(ChartStatusEnum.RUNNING.getText());
            boolean updateResult = chartService.updateById(updateChart);
            if (!updateResult) {
                channel.basicNack(deliveryTag, false, false);
                chartService.handlerChartUpdateError(updateChart.getId(), "更新 Chart 任务状态为进行中失败");
            }

            // AI 服务
            String result = aiManager.doChat(BiModelConstant.BI_MODEL_ID, buildUserInput(chart));
            String[] splits = result.split("【【【【【");
            if (splits.length != 3) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 分析异常");
            }

            // 返回数据格式处理
            String genChart = splits[1];
            String genResult = splits[2];
            genChart = chartService.RemoveFirstNewline(genChart);
            genResult = chartService.RemoveFirstNewline(genResult);
            // todo 生成输入的校验，使用正则处理不合法的字符，应对 AI 智障


            // 保存结果到数据库，并修改任务状态
            updateChart.setGenChart(genChart);
            updateChart.setGenResult(genResult);
            updateChart.setStatus(ChartStatusEnum.SUCCEED.getValue());
            updateChart.setExecuteMessage(ChartStatusEnum.SUCCEED.getText());
            boolean overBiUpdateResult = chartService.updateById(updateChart);
            if (!overBiUpdateResult) {
                channel.basicNack(deliveryTag, false, false);
                chartService.handlerChartUpdateError(updateChart.getId(), "保存 Chart 生成任务执行结果数据库失败");
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息拒绝失败");
        }

        // 任务执行成功 => 消息确认
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息签收失败");
        }
    }


    private String buildUserInput(Chart chart) {
        String chartType = chart.getChartType();
        String goal = chart.getGoal();
        String csvData = chart.getChartData();

        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求:").append("\n");
        if (StringUtils.isNotBlank(chartType)) {
            goal = goal + "，请使用" + chartType;
        }
        userInput.append(goal).append("\n");
        userInput.append("原始数据:").append("\n");
        userInput.append(csvData).append("\n");

        return userInput.toString();
    }
}
