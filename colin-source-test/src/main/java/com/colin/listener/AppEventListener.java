package com.colin.listener;

import com.colin.bean.cycle.A;
import org.aspectj.bridge.Message;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author colin
 * @create 2021-10-27 09:50
 *
 * 事件监听器原理：
 */
@Component
public class AppEventListener {

	public AppEventListener() {
		System.out.println("AppEventListener=============");
	}

	// 感知自定义事件
	@EventListener(MessageEvent.class)
	public void listenerMessage(MessageEvent event){
		System.out.println("Message事件到达..."+event+"；已发送邮件....");
	}

	// 感知自定义事件
	@EventListener(ChangeEvent.class)
	public void listenChange(ChangeEvent event){
		System.out.println("Change事件到达..."+event+"；已同步状态....");
	}

	// 感知任意对象事件的（Payload带负载的）
	@EventListener(PayloadApplicationEvent.class)
	public void listenPayload(PayloadApplicationEvent<A> event){
		System.out.println("Payload事件到达..."+event.getPayload()+"；已进行处理....");
	}
}
