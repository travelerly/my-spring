package com.colin.listener;

import org.springframework.context.ApplicationEvent;

import java.io.Serializable;

/**
 * @author colin
 * @create 2021-10-27 09:53
 *
 * 事件需要实现序列化接口
 *
 */
public class ChangeEvent extends ApplicationEvent implements Serializable {

	private static final long serialVersionUID = 0L;

	private String state;

	/**
	 * Create a new {@code ApplicationEvent}.
	 *
	 * @param source the object on which the event initially occurred or with
	 *               which the event is associated (never {@code null})
	 */
	public ChangeEvent(Object source) {
		super(source);
	}

	public ChangeEvent(Object source, String state) {
		super(source);
		this.state = state;
	}

	public String getState() {
		return state;
	}

	@Override
	public String toString() {
		return "ChangeEvent{" +
				"state='" + state + '\'' +
				", source=" + source +
				'}';
	}
}
