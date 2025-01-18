package io.pb.wi.projekt;

import java.util.concurrent.atomic.AtomicLong;

public class User {

    private static final AtomicLong counter = new AtomicLong(0);

    private final long id;
    private final String name;
    private final int age;
    private final String location;
    private final String profileUrl;

    public User(String name, int age, String location, String profileUrl) {
        this.id = counter.getAndIncrement();
        this.name = name;
        this.age = age;
        this.location = location;
        this.profileUrl = profileUrl;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getLocation() {
        return location;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (age != user.age) return false;
        if (!name.equals(user.name)) return false;
        if (!location.equals(user.location)) return false;
        return profileUrl.equals(user.profileUrl);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        result = 31 * result + age;
        result = 31 * result + location.hashCode();
        result = 31 * result + profileUrl.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", location='" + location + '\'' +
                ", profileUrl='" + profileUrl + '\'' +
                '}';
    }
}