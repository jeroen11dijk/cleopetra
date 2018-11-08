package dk.aau.cs.ds306e18.tournament.model;

import dk.aau.cs.ds306e18.tournament.model.format.Format;

import java.util.Objects;

public class Stage {

    private String name;
    private Format format;
    private int numberOfTeamsWanted = 16;

    public Stage(String name, Format format) {
        this.name = name;
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        if (this.format.getStatus() != StageStatus.PENDING)
            throw new IllegalStateException("Stage has already started.");
        this.format = format;
        System.out.println("format changed!");
    }

    public int getNumberOfTeamsWanted() {
        return numberOfTeamsWanted;
    }

    public void setNumberOfTeamsWanted(int numberOfTeamsWanted) {
        if (this.format.getStatus() != StageStatus.PENDING)
            throw new IllegalStateException("Stage has already started.");
        this.numberOfTeamsWanted = numberOfTeamsWanted;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stage stage = (Stage) o;
        return getNumberOfTeamsWanted() == stage.getNumberOfTeamsWanted() &&
                Objects.equals(getName(), stage.getName()) &&
                Objects.equals(getFormat(), stage.getFormat());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getFormat(), getNumberOfTeamsWanted());
    }
}
