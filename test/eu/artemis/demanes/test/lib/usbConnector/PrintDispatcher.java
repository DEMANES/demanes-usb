/**
 * File PrintDispatcher.java
 * 
 * This file is part of the eu.artemis.demanes.lib.usbConnector project.
 *
 * Copyright 2014 TNO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.artemis.demanes.test.lib.usbConnector;

import java.nio.ByteBuffer;

import aQute.bnd.annotation.component.Component;
import eu.artemis.demanes.lib.MessageDispatcher;
import eu.artemis.demanes.lib.impl.communication.CommUtils;

/**
 * PrintDispatcher
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 1 jul. 2014
 *
 */
@Component(immediate = true)
public class PrintDispatcher implements MessageDispatcher {

	/* (non-Javadoc)
	 * @see eu.artemis.demanes.lib.MessageDispatcher#dispatchMessage(java.nio.ByteBuffer)
	 */
	@Override
	public ByteBuffer dispatchMessage(ByteBuffer msgBuffer) {
		System.out.println(CommUtils.asHex(msgBuffer.array()));
		return null;
	}
	
}
