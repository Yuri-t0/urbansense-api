package br.com.urbanintel.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ALERTS_EXCHANGE     = "urban.alerts.exchange";

    public static final String ALERTS_QUEUE        = "urban.alerts.queue";
    public static final String AI_ALERTS_QUEUE     = "urban.ai.alerts.queue";

    public static final String ALERTS_DLX          = "urban.alerts.dlx";
    public static final String ALERTS_DLQ          = "urban.alerts.dlq";
    public static final String AI_ALERTS_DLQ       = "urban.ai.alerts.dlq";

    public static final String ROUTING_ALL         = "alert.#";
    public static final String ROUTING_SP          = "alert.#.sp";
    public static final String ROUTING_DLQ         = "dlq.alerts";

    private static final int MESSAGE_TTL_MS        = 86_400_000;

    @Bean
    DirectExchange alertsDlx() {
        return new DirectExchange(ALERTS_DLX, true, false);
    }

    @Bean
    Queue alertsDlq() {
        return QueueBuilder.durable(ALERTS_DLQ).build();
    }

    @Bean
    Queue aiAlertsDlq() {
        return QueueBuilder.durable(AI_ALERTS_DLQ).build();
    }

    @Bean
    Binding alertsDlqBinding(Queue alertsDlq, DirectExchange alertsDlx) {
        return BindingBuilder.bind(alertsDlq).to(alertsDlx).with(ROUTING_DLQ);
    }

    @Bean
    Binding aiAlertsDlqBinding(Queue aiAlertsDlq, DirectExchange alertsDlx) {
        return BindingBuilder.bind(aiAlertsDlq).to(alertsDlx).with("dlq.ai.alerts");
    }

    @Bean
    TopicExchange alertsExchange() {
        return new TopicExchange(ALERTS_EXCHANGE, true, false);
    }

    @Bean
    Queue alertsQueue() {
        return QueueBuilder.durable(ALERTS_QUEUE)
                
                .withArgument("x-dead-letter-exchange", ALERTS_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_DLQ)
                
                .withArgument("x-message-ttl", MESSAGE_TTL_MS)
                
                .withArgument("x-max-length", 10_000)
                .build();
    }

    @Bean
    Queue aiAlertsQueue() {
        return QueueBuilder.durable(AI_ALERTS_QUEUE)
                .withArgument("x-dead-letter-exchange", ALERTS_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq.ai.alerts")
                .withArgument("x-message-ttl", MESSAGE_TTL_MS)
                .build();
    }

    @Bean
    Binding alertsBinding(Queue alertsQueue, TopicExchange alertsExchange) {
        return BindingBuilder.bind(alertsQueue).to(alertsExchange).with(ROUTING_ALL);
    }

    @Bean
    Binding aiAlertsBinding(Queue aiAlertsQueue, TopicExchange alertsExchange) {
        return BindingBuilder.bind(aiAlertsQueue).to(alertsExchange).with(ROUTING_SP);
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter());
        
        template.setMandatory(true);
        return template;
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory cf) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(messageConverter());
        factory.setDefaultRequeueRejected(false); 
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }
}
