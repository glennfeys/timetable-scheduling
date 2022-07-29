package models;

import java.util.Objects;

public class Program {
    private String code;
    private String homesite;
    private int mt1;

    public String getCode() {
        return code;
    }

    public String getHomesite() {
        return homesite;
    }

    public int getMt1() {
        return mt1;
    }

    @Override
    public String toString() {
        return "(" + code + "," + mt1 + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Program program = (Program) o;
        return mt1 == program.mt1 &&
                code.equals(program.code) &&
                Objects.equals(homesite, program.homesite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, homesite, mt1);
    }
}
