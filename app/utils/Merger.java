package utils;

public interface Merger<A, B>
{
    A merge(A a,
            B b);
}
