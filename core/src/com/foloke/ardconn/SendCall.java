package com.foloke.ardconn;

import jssc.*;
import java.util.concurrent.Callable;

public class SendCall implements Callable<String> {
    private final SerialPort serialPort;

    private final String message;
    private boolean reading = false;

    private String output;

    public SendCall(SerialPort serialPort, String message) {
        this.serialPort = serialPort;
        this.message = message;
    }

    @Override
    public String call() {
        System.out.println("sending " + message + " on " + serialPort.getPortName());

        try {
            boolean writeResult = serialPort.writeString(message);
            if (!writeResult) {
                return "SENDING ERROR";
            }

            reading = true;

            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);

        } catch (Exception e) {
            return "SENDING ERROR: " + e.toString();
        }

        try {
            while (reading) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            output = "SENDING ERROR: " + e.toString();
        }

        try {
            serialPort.removeEventListener();
        } catch (Exception e) {
            System.out.println("can't close listener");
        }

        return output;
    }

    private class PortReader implements SerialPortEventListener {
        StringBuilder stringBuilder = new StringBuilder();

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    stringBuilder.append(serialPort.readString(event.getEventValue()));

                    if(stringBuilder.indexOf("\n") > 0) {
                        output = stringBuilder.toString().trim();
                        reading = false;
                    }
                }
                catch (SerialPortException e) {
                    System.out.println(e.toString());
                    output = "ERROR";
                    reading = false;
                }
            } else {
                System.out.println(event.getEventType());
            }
        }
    }
}
