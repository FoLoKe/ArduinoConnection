package com.foloke.ardconn;

import jssc.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.concurrent.*;

public class Manager {
    private static SerialPort serialPort;
    private final UI ui;

    enum Commands {
        RELOAD, DISARM, SHOOT, DISTANCE, NONE
    }

    Commands current = Commands.NONE;

    public Manager(UI ui) {
        this.ui = ui;

        try {
            //DEBUG
            //SessionFactory sessionFactory = new Configuration().configure()
            //        .buildSessionFactory();
            //Session session = sessionFactory.openSession();
            //session.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public String[] getPorts() {
        return SerialPortList.getPortNames();
    }

    public void connect(String port) {
        try {
            disconnect();

            serialPort = new SerialPort(port);
            serialPort.openPort();
            ui.output(serialPort.getPortName() + " connected\n");
            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE, false, true);

            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        } catch (Exception e) {
            ui.output(e.toString());
        }
    }

    public boolean inProcess;
    public String distance;

    class SendPacketTask implements Runnable {
        Commands command;

        public SendPacketTask(Commands command) {
            this.command = command;
        }

        @Override
        public void run() {
            if (!inProcess) {
                inProcess = true;

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Future<String> writeResult = executorService.submit(new SendCall(serialPort, command.toString()));
                ui.showOnWall("Подключение...");
                String response = null;

                if (serialPort != null && serialPort.isOpened()) {
                    try {
                        response = writeResult.get(5000, TimeUnit.MILLISECONDS);


                    } catch (Exception e) {
                        ui.output(e.toString() + "\n");
                        ui.showOnWall("Превышено время ожидания");
                    }
                } else {
                    ui.output("please connect to any COM\n");
                    ui.showOnWall("Сначала подключитесь");
                    executorService.shutdownNow();
                    ui.unblock();
                    inProcess = false;
                    return;
                }


                if (response != null) {
                    String reply = response.substring(0, response.indexOf(":"));
                    String value = response.substring(response.indexOf(":") + 1);

                    if (!reply.equals("ERROR")) {
                        ui.output(value + "\n");

                        switch (current) {
                            case RELOAD:
                            case DISARM:
                                ui.showOnWall("ОК");
                                ui.output(reply + "\n");
                                break;
                            case SHOOT:
                                ui.showOnWall("ОК");
                                ui.output(reply + "\n");
                                ui.askForHit();
                                break;
                            case DISTANCE:
                                ui.showOnWall(value + " м");
                                ui.output("Дистанция: " + value + " м\n");
                                distance = value;
                                break;
                            case NONE:
                                ui.output("YOU SHOULDN'T SEE THAT TASK NOT CLOSED CORRECTLY");
                                ui.showOnWall("ОШИБКА\n");
                                break;
                        }
                    } else {
                        ui.showOnWall("Ошибка: " + value);
                        ui.output(value);
                    }
                } else {
                    ui.showOnWall("ОШИБКА, попробуйте еще раз");
                    ui.output("error getting response\n");
                }

                executorService.shutdownNow();
                ui.unblock();

                inProcess = false;
            } else {
                ui.output("Already sending");
                ui.showOnWall("Пожалуйста подождите...");
            }
        }
    }

    private void sendPacket(Commands command) {
        new Thread(new SendPacketTask(command)).start();
    }

    public void disconnect() {
        try {
            if (serialPort != null && serialPort.isOpened()) {
                serialPort.closePort();
                ui.output(serialPort.getPortName() + " disconnected\n");
            }
        } catch (Exception e) {
            ui.output(e.toString());
        }
    }

    public void sendShootCommand() {
        send(Commands.SHOOT);
    }

    public void sendReloadCommand() {
        send(Commands.RELOAD);
    }

    public void sendCheckDistanceCommand() {
        send(Commands.DISTANCE);
    }

    public void sendDisarmCommand() {
        send(Commands.DISARM);
    }

    private void send(Commands command) {
        ui.block();
        current = command;
        sendPacket(command);
        ui.output(command + " command has been sent\n");
    }

    public void writeRecord(String name) {

        ui.unlockRecords();
        //TODO JDBC
    }
}
