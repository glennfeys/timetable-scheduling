package models;

import java.util.Objects;

public class Teacher {
    private String UGentid;

    public String getId() {
        return UGentid;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return Objects.equals(UGentid, teacher.UGentid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(UGentid);
    }
}
