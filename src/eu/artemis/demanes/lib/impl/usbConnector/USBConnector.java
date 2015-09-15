/**
 * File USBConnector.java
 * Created on 6 mei 2014 by oliveirafilhojad
 * 
 * This file was created for DEMANES project.
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

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import eu.artemis.demanes.exceptions.SocketConnectException;
import eu.artemis.demanes.lib.MessageDispatcher;
import eu.artemis.demanes.lib.MessageDispatcherRegistry;
import eu.artemis.demanes.lib.SocketConnector;
import eu.artemis.demanes.lib.impl.communication.CommUtils;
import eu.artemis.demanes.lib.impl.communication.MultiDispatcherServer;
import eu.artemis.demanes.lib.impl.communication.SocketReader;
import eu.artemis.demanes.logging.LogConstants;
import eu.artemis.demanes.logging.LogEntry;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * USBConnector
 * 
 * @author oliveirafilhojad
 * @version 0.1
 * @since 6 mei 2014
 * 
 */
@Component(immediate = true, designate = USBConnectorConfiguration.class, configurationPolicy = ConfigurationPolicy.require)
public class USBConnector implements SocketConnector {

	private final Logger logger = Logger.getLogger("dmns:log");

	private final MultiDispatcherServer server;

	private SerialPort serialPort;

	private Thread readerThread;

	private SocketReader reader;

	/**
	 * 
	 */
	public USBConnector() {
		this.server = new MultiDispatcherServer();
	}

	/**
	 * Try to make the connection. Will only do so if all the fields are
	 * appropriately set (The config and the messaageDispatcher)
	 * 
	 * @param properties
	 * @throws SocketConnectException
	 */
	@Activate
	public void connect(Map<?, ?> properties) throws SocketConnectException {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "LifeCycle", "Activating module"));

		USBConnectorConfiguration config = Configurable.createConfigurable(
				USBConnectorConfiguration.class, properties);

		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier
					.getPortIdentifier(config.getComPortName());

			logger.info(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_INFO, "Comm",
					"USB connector connecting to " + portIdentifier.getName()));

			if (portIdentifier.isCurrentlyOwned()) {
				logger.fatal(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_FATAL, "Comm", "Port "
								+ portIdentifier.getName() + " already in use"));

				throw new SocketConnectException("Port "
						+ portIdentifier.getName() + " already in use");
			} else if (portIdentifier.getPortType() != CommPortIdentifier.PORT_SERIAL) {
				logger.fatal(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_FATAL, "Comm",
						"Invalid port type, must be serial port, instead found "
								+ portIdentifier.getPortType()));

				throw new SocketConnectException(
						"Invalid port type, must be serial port, instead found "
								+ portIdentifier.getPortType());
			}

			serialPort = (SerialPort) portIdentifier.open(this.getClass()
					.getName(), config.getPortOpenTimeOut());
			serialPort.setSerialPortParams(config.getBitRate(),
					SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			reader = new SocketReader(serialPort.getInputStream(),
					serialPort.getOutputStream(), server);
			readerThread = new Thread(reader, "SocketReader thread for "
					+ serialPort.getName());
			readerThread.start();

		} catch (NoSuchPortException e) {
			logger.fatal(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_FATAL, "Comm", "No such port "
							+ config.getComPortName()));

			throw new SocketConnectException("No such port "
					+ config.getComPortName());
		} catch (PortInUseException e) {
			logger.fatal(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_FATAL, "Comm", "Port "
							+ config.getComPortName() + " in use"));

			throw new SocketConnectException("Port " + config.getComPortName()
					+ " is already in use");
		} catch (UnsupportedCommOperationException e) {
			logger.fatal(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_FATAL, "Comm",
					"Unsupported CommOperation", e));

			throw new SocketConnectException();
		} catch (IOException e) {
			logger.fatal(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_FATAL, "Comm",
					"IOException occured", e));

			throw new SocketConnectException(
					"Unable to get connect to serial port", e);
		}
	}

	@Modified
	public void modify(Map<?, ?> properties) throws SocketConnectException {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "LifeCycle", "Modifying module"));

		this.disconnect();
		this.connect(properties);
	}

	@Deactivate
	public void stop() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "LifeCycle",
				"Deactivating module"));

		this.disconnect();
	}

	/**
	 * This function is called whenever a Message dispatcher is available in the
	 * OSGI environment. It will try to connect to a communication socket an use
	 * the message dispatcher to communicate with the USB device.
	 * 
	 * @throws SocketConnectException
	 */
	@Reference(dynamic = true, optional = true, multiple = true)
	public void addDispatcher(MessageDispatcher dispatcher) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reference",
				"Adding MessageDispatcher " + dispatcher));

		this.server.addDispatcher(dispatcher);
	}

	public void removeDispatcher(MessageDispatcher dispatcher) {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Reference",
				"Removing MessageDispatcher " + dispatcher));

		this.server.removeDispatcher(dispatcher);
	}

	private void disconnect() {
		logger.debug(new LogEntry(this.getClass().getName(),
				LogConstants.LOG_LEVEL_DEBUG, "Comm", "Closing USB connection"));

		if (this.reader != null)
			this.reader.stop();

		if (this.readerThread != null)
			this.readerThread.interrupt();

		this.reader = null;
		this.readerThread = null;

		if (this.serialPort != null) {
			try {
				this.serialPort.getInputStream().close();
				this.serialPort.getOutputStream().close();
			} catch (IOException e) {
				logger.error(new LogEntry(this.getClass().getName(),
						LogConstants.LOG_LEVEL_ERROR, "Comm",
						"IOException occured", e));
			}
			this.serialPort.close();
			this.serialPort = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.artemis.demanes.lib.SocketConnector#write(java.nio.ByteBuffer)
	 */
	@Override
	public MessageDispatcherRegistry write(byte[] msg) {
		if (msg == null)
			return this.server;

		if (serialPort == null) {
			logger.warn(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_WARN, "Comm",
					"Unable to write to USB before connecting"));
			return this.server;
		}

		try {
			logger.trace(new LogEntry(this.getClass().getName(),
					LogConstants.LOG_LEVEL_TRACE, "Comm", "Sending via USB: "
							+ CommUtils.asHex(msg) + "0A"));

			// serialPort.getOutputStream().write((byte) msg.length);
			serialPort.getOutputStream().write(msg);
			serialPort.getOutputStream().write((byte) CommUtils.END_OF_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return server;
	}
}
