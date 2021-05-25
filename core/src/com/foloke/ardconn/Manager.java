package com.foloke.ardconn;

import jssc.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.List;
import org.hibernate.query.Query;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class Manager {
    private static SerialPort serialPort;
    public static final ArrayList<String> shells = new ArrayList<>(Arrays.asList("1g", "2g", "3g"));
    public final UI ui;

    enum Commands {
        RELOAD, DISARM, SHOOT, DISTANCE, NONE
    }

    Commands current = Commands.NONE;

    public Manager(UI ui) {
        this.ui = ui;
        distance = new Random().nextInt(4) * 100f;
        getRecords(10);
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
    public float distance;

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
                                value = value.substring(value.indexOf(":") + 1);
                                if ((value.matches("\\d*\\.\\d*"))) {
                                    distance = Float.parseFloat(value);

                                    StringBuilder sb = new StringBuilder();
                                    for (String shell: Manager.shells) {
                                        int angle = getRecommendation(5, shell, distance);
                                        sb.append(shell).append(": ").append(angle).append("°\n");
                                    }

                                    ui.showOnWall(distance / 100f + " м\n" + sb.toString());
                                    ui.output("Дистанция: " + value + " cm\n");
                                } else {
                                    distance = -1;
                                    ui.showOnWall( "Дистанция за границами");
                                    ui.output("Дистанция не верна:" + value + "\n");
                                }

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

    public boolean writeRecord(float angle, String choice) {
        if (distance > 0 && distance <= 400 && shells.contains(choice)) {
            try {
                Configuration configuration = new Configuration().configure();
                configuration.addAnnotatedClass(Record.class);
                SessionFactory sessionFactory = configuration
                        .buildSessionFactory();

                try (Session session = sessionFactory.openSession()) {
                    session.beginTransaction();
                    Record record = new Record();
                    record.setDistance(distance);
                    record.setAngle(angle);
                    record.setShell(choice);
                    session.save(record);
                    session.getTransaction().commit();
                    ui.output("successful write \n");

                } catch (Exception e) {
                    ui.output(e.toString());
                    return false;
                }
            } catch (Exception e) {
                ui.output(e.toString());
                return false;
            }
        } else {
            ui.output("distance or shell is not valid \n");
            return false;
        }
        return true;
    }

    public Integer getRecommendation(int count, String choice, float distance) {
        Integer recommendation = null;
        if (distance > 0 && distance <= 400) {
            try {
                Configuration configuration = new Configuration().configure();
                configuration.addAnnotatedClass(Record.class);
                SessionFactory sessionFactory = configuration
                        .buildSessionFactory();

                try (Session session = sessionFactory.openSession()) {
                    String countQ = "SELECT count(id) From Record Where distance >= :beg AND distance <= :end AND shell = :shell";
                    long countResults = 0;
                    int borders = 25;
                    for (int i = 0 ; i < 10 && countResults <= count; borders+=25, i++) {

                        TypedQuery<Long> countQuery = session.createQuery(countQ, Long.class);
                        countQuery.setParameter("beg", distance - borders);
                        countQuery.setParameter("shell", choice);
                        countQuery.setParameter("end", distance + borders);
                        countResults = countQuery.getSingleResult();
                    }
                    int lastPageNumber = (int) (Math.ceil(countResults / count));

                    TypedQuery<Record> selectQuery
                            = session.createQuery(
                            "From Record Where distance >= :beg AND distance <= :end AND shell = :shell"
                            , Record.class);
                    selectQuery.setParameter("beg", distance - borders);
                    selectQuery.setParameter("shell", choice);
                    selectQuery.setParameter("end", distance + borders);

                    int firstResult = Math.max(0, (lastPageNumber - 1) * count);
                    selectQuery.setFirstResult(firstResult);
                    selectQuery.setMaxResults(count);

                    List<Record> records = selectQuery.getResultList();
                    long avg = 0;
                    for(Record record : records) {
                        avg += record.getAngle();
                    }
                    avg = avg / records.size();

                    recommendation = (int) avg;
                    System.out.println(avg);
                    ui.output("successful read: " + avg +  "\n");

                } catch (Exception e) {
                    ui.output(e.toString());
                    e.printStackTrace();
                    return recommendation;
                }
            } catch (Exception e) {
                ui.output(e.toString());
                return recommendation;
            }
        } else {
            ui.output("distance is not valid\n");
            return recommendation;
        }
        return recommendation;
    }

    public List<Record> getRecords(int count) {
        List<Record> records = null;
        try {
            Configuration configuration = new Configuration().configure();
            configuration.addAnnotatedClass(Record.class);
            SessionFactory sessionFactory = configuration
                    .buildSessionFactory();

            try (Session session = sessionFactory.openSession()) {
                String countQ = "SELECT count(id) From Record";
                long countResults = 0;
                int borders = 25;
                for (int i = 0; i < 10 && countResults <= count; borders += 25, i++) {
                    TypedQuery<Long> countQuery = session.createQuery(countQ, Long.class);
                    countResults = countQuery.getSingleResult();
                }

                int lastPageNumber = (int) (Math.ceil(countResults / count));
                TypedQuery<Record> selectQuery = session.createQuery("From Record", Record.class);

                int firstResult = Math.max(0, (lastPageNumber - 1) * count);
                selectQuery.setFirstResult(firstResult);
                selectQuery.setMaxResults(count);

                records = selectQuery.getResultList();

                System.out.println(records.size());
                ui.output("successful read: " + records.size() + "\n");

            } catch (Exception e) {
                ui.output(e.toString());
                e.printStackTrace();
                return records;
            }
        } catch (Exception e) {
            ui.output(e.toString());
            return records;
        }

        return records;
    }
}
