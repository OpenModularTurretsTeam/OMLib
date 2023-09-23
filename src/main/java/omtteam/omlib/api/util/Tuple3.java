package omtteam.omlib.api.util;

import java.util.Objects;

public class Tuple3<A, B, C> extends Tuple<A, B> {
    private final C c;

    public Tuple3(A a, B b, C c) {
        super(a, b);
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;
        return c.equals(tuple3.c);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), c);
    }
}
