package com.javainuse.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	@Value("${javainuse.rabbitmq.queue}")
	String queueName;
	@Value("${javainuse.rabbitmq.dlq.queue}")
	String dlqName;
	@Value("${javainuse.rabbitmq.exchange}")
	String exchange;
	@Value("${javainuse.rabbitmq.dlq.exchange}")
	String dlqExchange;
	@Value("${javainuse.rabbitmq.dlq.routeKey}")
	String dlqRouteKey;
	@Value("${javainuse.rabbitmq.routingkey}")
	private String routingkey;

	@Bean
	Queue queue() {
		//return new Queue(queueName, false);
		return QueueBuilder.durable(queueName)
				.withArgument("x-dead-letter-exchange", dlqExchange)
				.withArgument("x-dead-letter-routing-key", dlqRouteKey)
				.build();
	}
	@Bean
	Queue dlq() {
		return QueueBuilder.durable(dlqName).build();
	}

	@Bean
	DirectExchange exchange() {
		return new DirectExchange(exchange);
	}
	@Bean
	DirectExchange deadLetterExchange() {
		return new DirectExchange(dlqExchange);
	}

	@Bean
	Binding binding(Queue queue, DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(routingkey);
	}
	@Bean
	Binding DLQbinding() {
		return BindingBuilder.bind(dlq()).to(deadLetterExchange()).with(dlqRouteKey);
	}
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
	@Bean
	public RabbitTemplate amqpTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}
}
