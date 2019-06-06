package omtteam.omlib.api.util;

import java.util.Objects;

public class Tuple<A, B> {
    protected A a;
    protected B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getFirst() {
        return a;
    }

    public B getSecond() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return a.equals(tuple.a) &&
                b.equals(tuple.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }
}
