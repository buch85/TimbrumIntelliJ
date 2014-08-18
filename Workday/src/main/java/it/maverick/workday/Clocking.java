package it.maverick.workday;

import java.util.Date;

/**
 * Created by Pasquale on 10/07/2014.
 */
public class Clocking {

    enum Direction {IN, OUT}

    private Date date;
    private Direction direction;

    public Clocking(Date date, Direction direction) {
        this.date = date;
        this.direction = direction;
    }

    public Date getDate() {
        return date;
    }

    public Direction getDirection() {
        return direction;
    }
}
