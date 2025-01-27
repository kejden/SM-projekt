package io.pb.wi.projekt;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class User {
    private static final AtomicLong counter = new AtomicLong(0);

    private final long id;
    private final String name;
    private final int age;
    private final String location;
    private final List<String> profileUrls;

    public User(String name, int age, String location, List<String> profileUrls) {
        this.id = counter.getAndIncrement();
        this.name = name;
        this.age = age;
        this.location = location;
        this.profileUrls = profileUrls;
    }

    public List<String> getProfileUrls() {
        return profileUrls;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                age == user.age &&
                name.equals(user.name) &&
                location.equals(user.location) &&
                profileUrls.equals(user.profileUrls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age, location, profileUrls);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", location='" + location + '\'' +
                ", profileUrls=" + profileUrls +
                '}';
    }
}