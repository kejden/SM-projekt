package io.pb.wi.projekt;

import java.util.concurrent.atomic.AtomicLong;

public class Spot {

    private static final AtomicLong counter = new AtomicLong(0);

    private final long id;
    private final String name;
    private final String city;
    private final String url;

    public Spot(String name, String city, String url) {
        this.id = counter.getAndIncrement();
        this.name = name;
        this.city = city;
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Spot spot = (Spot) o;

        if (id != spot.id) return false;
        if (!name.equals(spot.name)) return false;
        if (!city.equals(spot.city)) return false;
        return url.equals(spot.url);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        result = 31 * result + city.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Spot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
