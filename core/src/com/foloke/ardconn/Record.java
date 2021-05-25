package com.foloke.ardconn;

import javax.persistence.*;

import org.hibernate.annotations.DynamicUpdate;
import java.io.Serializable;

@Entity
@DynamicUpdate
@Table(name = "public.records", uniqueConstraints = {
        @UniqueConstraint(columnNames = "record_id")
})
public class Record implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "distance", nullable = false, precision = 3, scale = 2)
    private float distance;

    @Column(name = "angle", nullable = false, precision = 5, scale = 2)
    private float angle;

    @Column(name = "shell", nullable = false, length = 20)
    private String shell;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public String getShell() {
        return shell;
    }

    public void setShell(String shell) {
        this.shell = shell;
    }

    @Override
    public String toString() {
        return id + "\t " + distance / 100f + "м\t " + shell + "\t " + angle + "°";
    }
}
