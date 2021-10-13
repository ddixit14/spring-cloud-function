/*
 * Copyright 2020-2020 the original author or authors.
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

package org.springframework.cloud.function.context.catalog;

import java.util.function.BiFunction;

import org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry.FunctionInvocationWrapper;
import org.springframework.messaging.Message;

/**
 * Wrapper that acts as around advise over function invocation.
 * If registered as bean it will be autowired into {@link FunctionInvocationWrapper}.
 * Keep in mind that it only affects imperative invocations where input is {@link Message}
 *
 * NOTE: This API is experimental and and could change without notice. It is
 * intended for internal use only (e.g., spring-cloud-sleuth)
 *
 * @author Oleg Zhurakousky
 * @since 3.1
 */
public abstract class FunctionAroundWrapper implements BiFunction<Object, FunctionInvocationWrapper, Object> {

	@Override
	public final Object apply(Object input, FunctionInvocationWrapper targetFunction) {
		boolean isSkipOutputConversion = targetFunction.isSkipOutputConversion();
		targetFunction.setSkipOutputConversion(true);
		Object result = null;

		if (input instanceof Message || targetFunction.isOutputTypePublisher() || targetFunction.isInputTypePublisher()) {
			return this.doApply(input, targetFunction);
		}
		else if (targetFunction.isSupplier() && !targetFunction.isOutputTypePublisher()) {
			result = this.doApply(null, targetFunction);
		}
		else {
			result = targetFunction.apply(input);
		}
		targetFunction.setSkipOutputConversion(isSkipOutputConversion);
		return result;
	}

	protected abstract Object doApply(Object input, FunctionInvocationWrapper targetFunction);
}
