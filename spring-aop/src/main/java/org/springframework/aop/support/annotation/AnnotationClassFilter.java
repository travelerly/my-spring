/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.support.annotation;

import java.lang.annotation.Annotation;

import org.springframework.aop.ClassFilter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Simple ClassFilter that looks for a specific Java 5 annotation
 * being present on a class.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see AnnotationMatchingPointcut
 */
public class AnnotationClassFilter implements ClassFilter {

	private final Class<? extends Annotation> annotationType;

	private final boolean checkInherited;


	/**
	 * Create a new AnnotationClassFilter for the given annotation type.
	 * @param annotationType the annotation type to look for
	 */
	public AnnotationClassFilter(Class<? extends Annotation> annotationType) {
		// 默认情况下第二个参数为 false，即 checkInherited 为 false
		this(annotationType, false);
	}

	/**
	 * Create a new AnnotationClassFilter for the given annotation type.
	 * @param annotationType the annotation type to look for
	 * @param checkInherited whether to also check the superclasses and
	 * interfaces as well as meta-annotations for the annotation type
	 * (i.e. whether to use {@link AnnotatedElementUtils#hasAnnotation}
	 * semantics instead of standard Java {@link Class#isAnnotationPresent})
	 */
	// checkInherited = true，表示继承过来的注解也算
	public AnnotationClassFilter(Class<? extends Annotation> annotationType, boolean checkInherited) {
		Assert.notNull(annotationType, "Annotation type must not be null");
		this.annotationType = annotationType;
		this.checkInherited = checkInherited;
	}

	/**
	 * 匹配校验
	 * @param clazz the candidate target class
	 * @return true 表示能够匹配，则会进行织入的操作
	 */
	@Override
	public boolean matches(Class<?> clazz) {
		return (this.checkInherited ?
				// 继承的注解也会找出来进行校验
				AnnotatedElementUtils.hasAnnotation(clazz, this.annotationType) :
				// 只会看自己本类的注解
				clazz.isAnnotationPresent(this.annotationType));
	}

	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AnnotationClassFilter)) {
			return false;
		}
		AnnotationClassFilter otherCf = (AnnotationClassFilter) other;
		return (this.annotationType.equals(otherCf.annotationType) && this.checkInherited == otherCf.checkInherited);
	}

	@Override
	public int hashCode() {
		return this.annotationType.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + this.annotationType;
	}

}
