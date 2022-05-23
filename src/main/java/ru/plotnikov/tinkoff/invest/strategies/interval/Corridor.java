package ru.plotnikov.tinkoff.invest.strategies.interval;

import ru.plotnikov.tinkoff.invest.strategies.Action;

public class Corridor {

    private Long corridorMin;
    private Long corridorMax;
    private Action nextAction;

    public Corridor(Long corridorMin, Long corridorMax, Action nextAction) {
        this.corridorMin = corridorMin;
        this.corridorMax = corridorMax;
        this.nextAction = nextAction;
    }

    public Long getCorridorMin() {
        return corridorMin;
    }

    public Long getCorridorMax() {
        return corridorMax;
    }

    public Action getNextAction() {
        return nextAction;
    }

    public void setNextAction(Action nextAction) {
        this.nextAction = nextAction;
    }
}
