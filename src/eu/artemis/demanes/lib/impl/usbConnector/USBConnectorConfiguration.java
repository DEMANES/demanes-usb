/**
 * File USBConnectorConfiguration.java
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
package eu.artemis.demanes.lib.impl.usbConnector;

import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * USBConnectorConfiguration
 *
 * @author leeuwencjv
 * @version 0.1
 * @since 21 jun. 2014
 *
 */
@OCD(name = "Configuration for the USB Connection")
public interface USBConnectorConfiguration {

	@AD(name = "Com Port Name", description = "The name Com port to which the USB device is connected", deflt = "COM5", required = true)
	String getComPortName();
	
	@AD(name = "Bitrate", description = "The bitrate of the serial port", deflt = "115200", required = true, optionValues = {"75", "110", "300", "1200", "2400", "4800", "9600", "19200", "38400", "57600", "115200"})
	int getBitRate();
	
	@AD(name = "Open Timeout", description = "The timeout to open this port (in milliseconds)", deflt = "1000", required = true, min = "10")
	int getPortOpenTimeOut();

}
